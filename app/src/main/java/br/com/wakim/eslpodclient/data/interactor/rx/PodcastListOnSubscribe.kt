package br.com.wakim.eslpodclient.data.interactor.rx

import br.com.wakim.eslpodclient.data.model.PodcastItem
import br.com.wakim.eslpodclient.data.model.PodcastList
import br.com.wakim.eslpodclient.util.extensions.monthOfYear
import br.com.wakim.eslpodclient.util.extensions.onSuccessIfSubscribed
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.threeten.bp.LocalDate
import rx.Single
import rx.SingleSubscriber
import java.util.*

class PodcastListOnSubscribe(val url: String): Single.OnSubscribe<PodcastList> {

    companion object {
        const val PODCAST_TABLE_HOME_CLASS = ".podcast_table_home"
        const val DATE_SELECTOR = ".date-header"
        const val BODY_SELECTOR = "span.pod_body"
        const val TITLE_SELECTOR = "a.podcast_title"
        const val DESCRIPTION_SELECTOR = "strong ~ .pod_body .pod_body:gt(0)"
        const val TAGS_SELECTOR = "strong ~ .pod_body > a"
        const val PAGINATION_NEXT_SELECTOR = ".podcast_table_home:first-of-type td:eq(3) font a"
        const val ENGLISH_CAFE_TYPE_PREFIX = "EC"
    }

    override fun call(subscriber: SingleSubscriber<in PodcastList>) {
        val document = Jsoup.connect(url).get()
        val elements = document.select(PODCAST_TABLE_HOME_CLASS)
        val podcastList = PodcastList()
        val size = elements.size

        podcastList.list = ArrayList<PodcastItem>(size - 2)

        elements.asSequence()
                .filterIndexed { i, element -> i > 0 && i < (size - 1) }
                .map { buildPodcastItem(it) }
                .filter { it != null }
                .forEach { item ->
                    podcastList.list.add(item!!)
                }

        podcastList.currentPageToken = url
        podcastList.nextPageToken = getNextPage(document)

        subscriber.onSuccessIfSubscribed(podcastList)
    }

    fun buildPodcastItem(rootElement: Element) : PodcastItem? {
        val bodyElement = rootElement.select(BODY_SELECTOR).first()
        val titleElement = bodyElement.select(TITLE_SELECTOR).first()

        val remoteId = getRemoteId(titleElement)
        val title = getTitle(titleElement)
        val description = getDescription(bodyElement)
        val tags = getTags(bodyElement)
        val mp3Url = getMp3Url(bodyElement)
        val date = getDate(rootElement)
        val type = getType(mp3Url)

        return PodcastItem(remoteId, title, mp3Url, description, date, tags, type)
    }

    fun getTitle(root: Element) : String = root.text()

    fun getRemoteId(root: Element) : Long {
        val href = root.attr("href")
        return href.substring(href.indexOf("?issue_id=") + 10).toLong()
    }

    fun getDescription(root: Element) : String =
        (root.select(DESCRIPTION_SELECTOR).first().siblingNodes()[5] as TextNode).text().trim()

    fun getTags(root: Element) : String? = root.select(TAGS_SELECTOR).joinToString(", ") { it.text() }

    fun getMp3Url(root: Element) : String = root.select("a")[3].attr("href")

    @PodcastItem.Type
    fun getType(mp3Url: String) : Long {
        val fileName = mp3Url.split("/").last()

        return if (fileName.startsWith(ENGLISH_CAFE_TYPE_PREFIX)) PodcastItem.ENGLISH_CAFE else PodcastItem.PODCAST
    }

    fun getDate(root: Element) : LocalDate? {
        val dateElement = root.select(DATE_SELECTOR).firstOrNull()

        dateElement?.let {
            val parts = it.text().split(" ", "-", ",")

            val monthOfYear = parts[3].monthOfYear() + 1
            val dayOfMonth = parts[4].toInt()
            val year = parts[6].toInt()

            return LocalDate.of(year, monthOfYear, dayOfMonth)
        }

        return null
    }

    fun getNextPage(document: Document) : String?  =
        document.select(PAGINATION_NEXT_SELECTOR).firstOrNull()?.attr("abs:href")
}