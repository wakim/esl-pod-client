/**
 * Copyright 2013 Michał Charmas

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.charmas.android.tagview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.widget.TextView
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.util.extensions.dp

class TagView : TextView {

    companion object {
        private const val DEFAULT_PADDING = 8
        private const val DEFAULT_CORNER_RADIUS = 6F
        private val DEFAULT_COLOR = Color.parseColor("#DDDDDD")
        private const val DEFAULT_UPPERCASE = true

        private const val DEFAULT_SEPARATOR = " "
    }

    private var tagPadding: Int? = DEFAULT_PADDING
    private var tagCornerRadius: Float? = DEFAULT_CORNER_RADIUS

    private var tagColor: Int? = DEFAULT_COLOR
    private var tagSeparator: String? = DEFAULT_SEPARATOR

    private var isUppercaseTags = DEFAULT_UPPERCASE

    private var prefix = ""

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, R.attr.tagViewStyle)
    }

    constructor(context: Context?) : super(context)

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.TagView, defStyleAttr, R.style.Widget_TagView).apply {

                tagPadding = getDimensionPixelSize(R.styleable.TagView_tagPadding, context.dp(DEFAULT_PADDING))
                tagCornerRadius = getDimensionPixelSize(R.styleable.TagView_tagCornerRadius, context.dp(DEFAULT_CORNER_RADIUS).toInt()).toFloat()
                tagColor = getColor(R.styleable.TagView_tagColor, DEFAULT_COLOR)
                tagSeparator = getString(R.styleable.TagView_tagSeparator)

                isUppercaseTags = getBoolean(R.styleable.TagView_tagUppercase, DEFAULT_UPPERCASE)

                if (hasValue(R.styleable.TagView_tagPrefix)) {
                    prefix = getString(R.styleable.TagView_tagPrefix)
                }
            }.recycle()
        } else {
            tagPadding = context.dp(DEFAULT_PADDING)
            tagCornerRadius = context.dp(DEFAULT_CORNER_RADIUS)
        }
    }

    override fun setText(text: CharSequence?, type: TextView.BufferType) {
        if (text == null) {
            super.setText(null, type)
        } else {
            if (!isInEditMode)
                setTags(text.toString().split(tagSeparator ?: DEFAULT_SEPARATOR))
        }
    }

    fun setTags(tags: List<String>) {
        val separator = tagSeparator ?: DEFAULT_SEPARATOR
        setTags(tags, tagColor ?: DEFAULT_COLOR, separator)
    }

    fun setTags(tags: List<String>, @ColorInt color: Int, separator: String, type: TextView.BufferType = TextView.BufferType.NORMAL) {
        if (tags.size == 0) {
            super.setText(null, type)
            return
        }

        val sb = SpannableStringBuilder()

        var i = 0
        val size = tags.size

        while (i < size) {
            if (tags[i].isBlank()) {
                ++i
                continue
            }

            val tag = prefix + tags[i].trim()
            val tagContent = if (isUppercaseTags) tag.toUpperCase() else tag

            sb.append(tagContent).setSpan(
                    createSpan(tagContent, color),
                    sb.length - tagContent.length,
                    sb.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            if (i + 1 < size) {
                sb.append(separator)
            }

            ++i
        }

        super.setText(sb, type)
    }

    private fun createSpan(text: String, color: Int): TagSpan {
        return TagSpan(
                text,
                tagPadding ?: DEFAULT_PADDING,
                textSize,
                typeface === Typeface.DEFAULT_BOLD,
                currentTextColor,
                color,
                tagCornerRadius?.toFloat() ?: DEFAULT_CORNER_RADIUS)
    }

    class TagSpan(val text: String, tagPadding: Int, textSize: Float, bold: Boolean, textColor: Int, tagColor: Int, roundCornersFactor: Float) : ImageSpan(TagDrawable(text, tagPadding, textSize, bold, textColor, tagColor, roundCornersFactor))

    class TagDrawable (private val text: String, tagPadding: Int, textSize: Float, bold: Boolean, textColor: Int, tagColor: Int, private val roundCornersFactor: Float) : Drawable() {
        private val textContentPaint: Paint
        private val backgroundPaint: Paint
        private val fBounds: RectF
        private val backgroundPadding: Rect

        init {
            this.backgroundPadding = Rect(tagPadding, tagPadding, tagPadding, tagPadding)
            this.textContentPaint = Paint()

            textContentPaint.color = textColor
            textContentPaint.textSize = textSize
            textContentPaint.isAntiAlias = true
            textContentPaint.isFakeBoldText = bold
            textContentPaint.style = Paint.Style.FILL
            textContentPaint.textAlign = Paint.Align.LEFT

            this.backgroundPaint = Paint()

            backgroundPaint.color = tagColor
            backgroundPaint.style = Paint.Style.FILL
            backgroundPaint.isAntiAlias = true

            setBounds(0, 0,
                    textContentPaint.measureText(text).toInt() + backgroundPadding.left + backgroundPadding.right,
                    (textContentPaint.textSize + backgroundPadding.top.toFloat() + backgroundPadding.bottom.toFloat()).toInt())

            fBounds = RectF(bounds)

            fBounds.top += (tagPadding / 2).toFloat()
        }

        override fun draw(canvas: Canvas) {
            canvas.drawRoundRect(fBounds, roundCornersFactor, roundCornersFactor, backgroundPaint)
            canvas.drawText(text, (backgroundPadding.left + MAGIC_PADDING_LEFT).toFloat(), textContentPaint.textSize + backgroundPadding.top, textContentPaint)
        }

        override fun setAlpha(alpha: Int) {
            textContentPaint.alpha = alpha
            backgroundPaint.alpha = alpha
        }

        override fun setColorFilter(cf: ColorFilter?) {
            textContentPaint.colorFilter = cf
        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSLUCENT
        }

        companion object {
            private const val MAGIC_PADDING_LEFT = 0
        }
    }
}