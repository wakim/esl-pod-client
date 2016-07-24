package br.com.wakim.eslpodclient.data.interactor.rx

import br.com.wakim.eslpodclient.data.model.PodcastItem
import br.com.wakim.eslpodclient.data.model.PodcastItemDetail
import br.com.wakim.eslpodclient.data.model.SeekPos
import br.com.wakim.eslpodclient.util.extensions.secondsFromHourMinute
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import rx.Single
import rx.SingleSubscriber

class PodcastDetailOnSubscribe(val podcastItem: PodcastItem, val itemUrl: String) : Single.OnSubscribe<PodcastItemDetail> {

    companion object {
        const val PODCAST_BODY_CLASS = ".podcast_table_home .pod_body"
    }

    override fun call(subscriber: SingleSubscriber<in PodcastItemDetail>?) {
        val document = Jsoup.connect(itemUrl).get()
        val bodies = document.select(PODCAST_BODY_CLASS)
        val podcastDetail = PodcastItemDetail(type = podcastItem.type)

        val seekPos = getSeekPositions(bodies[0])
        val script = getScript(bodies[1], bodies[2])

        podcastDetail.script = script
        podcastDetail.seekPos = seekPos
        podcastDetail.remoteId = podcastItem.remoteId
        podcastDetail.title = podcastItem.title

        subscriber?.let {
            if (!it.isUnsubscribed)
                it.onSuccess(podcastDetail)
        }
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