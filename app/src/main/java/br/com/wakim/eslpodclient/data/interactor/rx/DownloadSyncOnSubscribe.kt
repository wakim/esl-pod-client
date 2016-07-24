package br.com.wakim.eslpodclient.data.interactor.rx

import br.com.wakim.eslpodclient.data.interactor.DownloadDbInteractor
import br.com.wakim.eslpodclient.data.interactor.StorageInteractor
import br.com.wakim.eslpodclient.data.model.DownloadStatus
import br.com.wakim.eslpodclient.data.model.PodcastItem
import br.com.wakim.eslpodclient.data.model.PodcastItemDetail
import br.com.wakim.eslpodclient.data.model.SeekPos
import br.com.wakim.eslpodclient.util.extensions.getFileName
import br.com.wakim.eslpodclient.util.extensions.onNextIfSubscribed
import br.com.wakim.eslpodclient.util.extensions.secondsFromHourMinute
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import rx.Observable
import rx.Subscriber

class DownloadSyncOnSubscribe(private val storageInteractor: StorageInteractor,
                              private val downloadDbInteractor: DownloadDbInteractor,
                              private val url: String): Observable.OnSubscribe<Pair<PodcastItem, PodcastItemDetail>> {

    companion object {
        const val RESULTS_SELECTOR = "#res"
        const val RESULT_SELECTOR = "li div.g"
        const val LINK_SELECTOR = "a.l"
        const val PODCAST_TITLE_SELECTOR = "title"
        const val PODCAST_DATE_SELECTOR = ".date-header"
        const val PODCAST_BODIES_SELECTOR = ".pod_body"
        const val PODCAST_TAGS_SELECTOR = "a:gt(4)"
    }

    override fun call(subscriber: Subscriber<in Pair<PodcastItem, PodcastItemDetail>>) {
        val baseDir = storageInteractor.getBaseDir()

        downloadDbInteractor.clearDatabase()

        baseDir.listFiles().asSequence()
                .map { file -> file.nameWithoutExtension }
                .filter { filename -> downloadDbInteractor.getDownloadByFilename(filename) == null }
                .filter { filename -> filename.startsWith("EC") || filename.startsWith("ESLPod") }
                .map { filename -> filename.replace("EC", "English Cafe ").replace("ESLPod", "ESLPod ") }
                .map { searchQuery -> searchAndUpdate(searchQuery, if (searchQuery.startsWith("English Cafe")) PodcastItem.ENGLISH_CAFE else PodcastItem.PODCAST) }
                .filterNotNull()
                .forEach { pair ->
                    subscriber.onNextIfSubscribed(pair)
                }

        subscriber.onCompleted()
    }

    fun searchAndUpdate(searchQuery: String, type: Long): Pair<PodcastItem, PodcastItemDetail>? {
        val document = Jsoup.connect(url + searchQuery).get()
        val body = document.select(RESULTS_SELECTOR)

        val first = body.select(RESULT_SELECTOR).firstOrNull()

        return first?.let {
            val link = first.select(LINK_SELECTOR).first()
            savePodcastInfo(link.absUrl("href"), type)
        } ?: null
    }

    fun savePodcastInfo(url: String, type: Long): Pair<PodcastItem, PodcastItemDetail> {
        val remoteId = url.substring(url.indexOf("?issue_id=") + 10).toLong()
        val document = Jsoup.connect(url).get()
        val titleEl = document.select(PODCAST_TITLE_SELECTOR).first()
        val body = titleEl.parent()
        val dateEl = body.select(PODCAST_DATE_SELECTOR).first()
        val podBodies = body.select(PODCAST_BODIES_SELECTOR)

        val title = titleEl.text().trim()
        val date = LocalDate.parse(dateEl.text(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val seekPos = getSeekPositions(podBodies[2])
        val script = getScript(podBodies[3], podBodies[4])
        val mp3Url = podBodies[0].select("a")[2].attr("href")
        val tags = podBodies[0].select(PODCAST_TAGS_SELECTOR).joinToString(", ") { it.text() }

        val podcastDetail = PodcastItemDetail(remoteId = remoteId, type = type, script = script, seekPos = seekPos, title = title)
        val podcastItem = PodcastItem(remoteId = remoteId, title = title, date = date, mp3Url = mp3Url, tags = tags, type = type)

        downloadDbInteractor.insertDownload(filename = podcastItem.mp3Url.getFileName(), remoteId = remoteId, status = DownloadStatus.DOWNLOADED, downloadId = 0L)

        return podcastItem to podcastDetail
    }

    fun getSeekPositions(indexBody : Element) : SeekPos? {
        val textNodes = indexBody.textNodes()

        if (textNodes.size < 4)
            return null

        val slow = textNodes[1].text().trim()
        val explanation = textNodes[2].text().trim()
        val normal = textNodes[3].text().trim()

        return SeekPos(extractSeekInSeconds(slow), extractSeekInSeconds(explanation), extractSeekInSeconds(normal))
    }

    fun extractSeekInSeconds(label : String) : Int {
        val indexOfFirstDoubleDot = label.indexOf(":")
        val time = label.substring(indexOfFirstDoubleDot + 1).trim()

        return time.secondsFromHourMinute()
    }

    fun getScript(body1 : Element, body2 : Element) : String {
        val body2Text = body2.text()
        return body1.html() + if (!body2Text.isBlank()) ("\n\n" + body2Text) else ""
    }
}