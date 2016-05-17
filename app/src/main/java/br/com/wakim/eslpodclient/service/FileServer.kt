package br.com.wakim.eslpodclient.service

import br.com.wakim.eslpodclient.BuildConfig
import br.com.wakim.eslpodclient.extensions.getFileName
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class FileServer(private val baseDir : String) : NanoHTTPD(BuildConfig.SERVER_PORT) {

    override fun serve(uri : String, method : Method, header : Map<String, String>, parameters : Map<String, String>, files : Map<String, String>) : Response {
        val f = File("$baseDir${File.separator}${uri.getFileName()}")
        return serveFile(uri, header, f, "audio/mpeg")
    }

    /**
     * Serves file from homeDir and its' subdirectories (only). Uses only URI,
     * ignores all headers and HTTP parameters.
     */
    fun serveFile(uri : String, header: Map<String, String>, file : File, mime: String) : Response {
        val res: Response?

        try {
            // Calculate etag
            val etag = Integer.toHexString((file.absolutePath + file.lastModified() + "" + file.length()).hashCode())

            // Support (simple) skipping:
            var startFrom = 0L
            var endAt = -1L
            var range = header["range"]

            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length)

                    val minus = range.indexOf('-')

                    try {
                        if (minus > 0) {
                            startFrom = range.substring(0, minus).toLong()
                            endAt = range.substring(minus + 1).toLong()
                        }
                    } catch (ignored : NumberFormatException) { }
                }
            }

            // get if-range header. If present, it must match etag or else we
            // should ignore the range request
            val ifRange = header["if-range"]
            val headerIfRangeMissingOrMatching = (ifRange == null || etag == ifRange)

            val ifNoneMatch = header["if-none-match"]
            val headerIfNoneMatchPresentAndMatching = ifNoneMatch != null && ("*" == ifNoneMatch || ifNoneMatch == etag)

            // Change return code and add Content-Range header when skipping is
            // requested
            val fileLen = file.length()

            if (headerIfRangeMissingOrMatching && range != null && startFrom >= 0 && startFrom < fileLen) {
                // range request that matches current etag
                // and the startFrom of the range is satisfiable
                if (headerIfNoneMatchPresentAndMatching) {
                    // range request that matches current etag
                    // and the startFrom of the range is satisfiable
                    // would return range from file
                    // respond with not-modified
                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "")
                    res.addHeader("ETag", etag)
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1
                    }

                    var newLen = endAt - startFrom + 1

                    if (newLen < 0) {
                        newLen = 0
                    }

                    val fis = FileInputStream(file)

                    fis.skip(startFrom)

                    res = newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, mime, fis, newLen)

                    res.addHeader("Accept-Ranges", "bytes")
                    res.addHeader("Content-Length", "$newLen")
                    res.addHeader("Content-Range", "bytes $startFrom-$endAt/$fileLen")
                    res.addHeader("ETag", etag)
                }
            } else {

                if (headerIfRangeMissingOrMatching && range != null && startFrom >= fileLen) {
                    // return the size of the file
                    // 4xx responses are not trumped by if-none-match
                    res = newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "")
                    res.addHeader("Content-Range", "bytes */$fileLen")
                    res.addHeader("ETag", etag)
                } else if (range == null && headerIfNoneMatchPresentAndMatching) {
                    // full-file-fetch request
                    // would return entire file
                    // respond with not-modified
                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "")
                    res.addHeader("ETag", etag)
                } else if (!headerIfRangeMissingOrMatching && headerIfNoneMatchPresentAndMatching) {
                    // range request that doesn't match current etag
                    // would return entire (different) file
                    // respond with not-modified

                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "")
                    res.addHeader("ETag", etag);
                } else {
                    // supply the file
                    res = newFixedFileResponse(file, mime)
                    res.addHeader("Content-Length", "$fileLen")
                    res.addHeader("ETag", etag)
                }
            }
        } catch (ioe: IOException ) {
            res = getForbiddenResponse("Reading file failed.")
        }

        return res!!
    }

    private fun newFixedFileResponse(file: File, mime: String) : Response {
        val res = newFixedLengthResponse(Response.Status.OK, mime, FileInputStream(file), file.length())

        res.addHeader("Accept-Ranges", "bytes")

        return res
    }

    protected fun getForbiddenResponse(s: String) : Response {
        return newFixedLengthResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: $s")
    }
}