package com.example.newpuzzle

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.newpuzzle.databinding.ActivityMainBinding
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    data class DrawableStructure(
        var initialPosition: String,
        var currentPosition: String,
        var drawable: Drawable?
    )

    private lateinit var layout: ActivityMainBinding
    private lateinit var finalDrawable: BitmapDrawable
    private var tileWidth: Int = 0
    private var tileHeight: Int = 0
    private var emptyTile: String = "33"
    private lateinit var queue: RequestQueue
    private var random: Random = Random()
    private var score: Int = 0

    private var viewIds: HashMap<Int, String> = HashMap()
    private var imageviews: HashMap<String, ImageView> = HashMap();
    private val drawableStructures: HashMap<String, DrawableStructure> = HashMap()
    private lateinit var theTimer: CountDownTimer;


    private fun initializeBitmap(originalBitmap: Bitmap) {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        tileWidth = width / 4;tileHeight = height / 4
        var bmp = BitmapDrawable(resources, originalBitmap)
        val bitmapResized = Bitmap.createScaledBitmap(originalBitmap, 2000, 1100, false)
        finalDrawable = BitmapDrawable(resources, bitmapResized)
    }

    fun shiftOnce() {
        var a = Character.getNumericValue(emptyTile[0]!!)
        var b = Character.getNumericValue(emptyTile[1]!!)
        var moves = ArrayList<String>()

        var c = a - 1;
        var d = b;
        if (bounds(c, d)) {
            moves.add("".plus(c).plus(d))
        }

        c = a + 1; d = b
        if (bounds(c, d)) {
            moves.add("".plus(c).plus(d))
        }

        c = a; d = b - 1
        if (bounds(c, d)) {
            moves.add("".plus(c).plus(d))
        }

        c = a; d = b + 1
        if (bounds(c, d)) {
            moves.add("".plus(c).plus(d))
        }

        var next = Random().nextInt(moves.size)
        exchangeIfNull(emptyTile, moves[next])
    }

    fun bounds(a: Int, b: Int): Boolean {
        return ((a in 0..3) && (b in 0..3))
    }

    fun shuffle(difficulty: String) {
        when (difficulty) {
            "easy" -> {
                for (times in 0..5)
                    shiftOnce()
            }
            "medium" -> {
                for (times in 0..10)
                    shiftOnce()
            }
            "hard" -> {
                for (times in 0..20)
                    shiftOnce()
            }
            "extreme" -> {
                for (times in 0..30)
                    shiftOnce()
            }
            else -> {
            }
        }
    }

    fun setMargins(v: View, l: Int, t: Int, r: Int, b: Int) {
        if (v.layoutParams is MarginLayoutParams) {
            val p = v.layoutParams as MarginLayoutParams
            p.setMargins(l, t, r, b)
            v.requestLayout()
        }
    }

    fun createImageContainer(): LinearLayout {
        var imageContainer = LinearLayout(this);
        imageContainer.layoutParams =
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f)
        setMargins(imageContainer, 0, 0, 0, 0)
        imageContainer.weightSum = 4.0f
        imageContainer.orientation = LinearLayout.VERTICAL
        return imageContainer
    }

    fun clearBoard() {
        emptyTile = "33"
        for (i in 0..3) {
            for (j in 0..3) {
                var customBitmap: Bitmap = Bitmap.createBitmap(
                    finalDrawable.bitmap,
                    i * tileWidth,
                    j * tileHeight,
                    tileWidth,
                    tileHeight
                )
                val customDrawable = BitmapDrawable(resources, customBitmap)
                drawableStructures["$i$j"] = DrawableStructure("$i$j", "$i$j", customDrawable)
            }
        }
        drawableStructures[emptyTile]?.drawable = null
        sync()
    }


    fun startTimer() {
        theTimer.start()
    }

    fun refresh(view: View) {
        clearBoard()
        startTimer()
    }

    fun shuffleButtonClick(view: View) {
        if (layout.shuffleContainer.visibility == View.VISIBLE) {
            layout.shuffleContainer.visibility = View.INVISIBLE
        } else if (layout.shuffleContainer.visibility == View.INVISIBLE) {
            layout.shuffleContainer.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layout = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(layout.root)
        supportActionBar?.hide()
        theTimer = object : CountDownTimer(400000, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                var totalSeconds = millisUntilFinished / 1000
                var min = totalSeconds / 60
                var sec = totalSeconds - (60 * min)

                var m: String = "".plus(min)
                if (min < 10) {
                    m = "0".plus(m)
                }

                var s: String = "".plus(sec)
                if (sec < 10) {
                    s = "0".plus(s)
                }

                var time = "".plus(m).plus(":").plus(s)

                layout.timer.setText(time)
            }

            override fun onFinish() {
            }
        };
        queue = Volley.newRequestQueue(this)
        downloadAndInitialize()
    }


    fun downloadAndInitialize(view: View) {
        downloadAndInitialize()
    }

    fun shuffleeasy(view: View) {
        shuffle("easy"); shuffleButtonClick(View(this))
    }

    fun shufflemedium(view: View) {
        shuffle("medium"); shuffleButtonClick(View(this))
    }

    fun shufflehard(view: View) {
        shuffle("hard"); shuffleButtonClick(View(this))
    }

    fun shuffleextreme(view: View) {
        shuffle("extreme"); shuffleButtonClick(View(this))
    }


    fun downloadAndInitialize() {
        layout.topbar.findViewById<ImageView>(R.id.next).visibility = View.INVISIBLE
        layout.topbar.findViewById<ProgressBar>(R.id.progress).visibility = View.VISIBLE
        // VOLLEY REQUEST
        var splashURL =
            "https://api.unsplash.com/search/photos/?client_id=Iafh-U0zzm81f22G4MmJUnPFA2LuGjTM3-HJfe-nVY4&orientation=landscape&query=nature"
        val stringRequest = StringRequest(Request.Method.GET, splashURL,
            Response.Listener<String> { response ->

                val reader = JSONObject(response)
                var objects = reader.getJSONArray("results")
                var zero = objects.getJSONObject(random.nextInt(objects.length()));
                var urls = zero.getJSONObject("urls")
                var raw = urls.getString("regular")


                val stringRequest = ImageRequest(raw,
                    Response.Listener<Bitmap> { response ->
                        initializeBitmap(response)
                        initializeBoard()
                        layout.topbar.findViewById<ProgressBar>(R.id.progress).visibility =
                            View.INVISIBLE
                        layout.topbar.findViewById<ImageView>(R.id.next).visibility = View.VISIBLE
                    }, 10000, 10000, ImageView.ScaleType.CENTER, Bitmap.Config.ALPHA_8,
                    Response.ErrorListener {
                        Toast.makeText(
                            this,
                            "Network error while downloading image!",
                            Toast.LENGTH_LONG
                        )
                    })

                queue.add(stringRequest)
            },
            Response.ErrorListener {
                Toast.makeText(this, "Network error while downloading image!", Toast.LENGTH_LONG)
            })
        queue.add(stringRequest)
    }


    fun initializeBoard() {
        layout.container.removeAllViews()
        for (i in 0..3) {
            var imageContainer = createImageContainer()
            for (j in 0..3) {
                var f = imageViewWrapper()
                val imageViewId = View.generateViewId()

                class MyOnSwipe(ctx: Context?) : OnSwipeTouchListener(ctx) {
                    override fun onSwipeBottom() {
                        var arr = getAandB(initiatingView);
                        var a = arr[0];
                        var b = arr[1];
                        b++
                        var possibleMove = "$a$b"
                        exchangeIfNull(possibleMove, viewIds[initiatingView.id]!!, true)
                    }


                    override fun onSwipeLeft() {
                        var arr = getAandB(initiatingView);
                        var a = arr[0];
                        var b = arr[1];
                        a--
                        var possibleMove = "$a$b"
                        exchangeIfNull(possibleMove, viewIds[initiatingView.id]!!, true)
                    }

                    override fun onSwipeTop() {
                        var arr = getAandB(initiatingView);
                        var a = arr[0];
                        var b = arr[1];
                        b--
                        var possibleMove = "$a$b"
                        exchangeIfNull(possibleMove, viewIds[initiatingView.id]!!, true)
                    }

                    override fun onSwipeRight() {
                        var arr = getAandB(initiatingView);
                        var a = arr[0];
                        var b = arr[1];
                        a++
                        var possibleMove = "$a$b"
                        exchangeIfNull(possibleMove, viewIds[initiatingView.id]!!, true)
                    }
                }

                var customBitmap: Bitmap = Bitmap.createBitmap(
                    finalDrawable.bitmap,
                    i * tileWidth,
                    j * tileHeight,
                    tileWidth,
                    tileHeight
                )
                val customDrawable = BitmapDrawable(resources, customBitmap)
                drawableStructures["$i$j"] = DrawableStructure("$i$j", "$i$j", customDrawable)


                var v = buildImageView(imageViewId, MyOnSwipe(this), customDrawable)
                viewIds[imageViewId] = "$i$j"
                imageviews["$i$j"] = v

                f.addView(v)
                imageContainer.addView(f)
            }
            layout.container.addView(imageContainer)
        }
        drawableStructures[emptyTile]?.drawable = null
        sync()
        shuffle("easy")
    }


    fun imageViewWrapper(): FrameLayout {
        var f = FrameLayout(this);
        f.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f)
        f.setPadding(2, 2, 2, 2)
        return f
    }

    fun buildImageView(imageViewId: Int, myOnSwipe: OnSwipeTouchListener, customDrawable: BitmapDrawable): ImageView {
        var v = ImageView(this)
        v.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        v.scaleType = ImageView.ScaleType.CENTER_CROP
        v.id = imageViewId
        v.setOnTouchListener(myOnSwipe)
        v.setImageDrawable(customDrawable)
        return v
    }

    fun getAandB(initiatingView: ImageView): Array<Int> {
        var a = Character.getNumericValue(viewIds[initiatingView.id]?.toCharArray()?.get(0)!!)
        var b = Character.getNumericValue(viewIds[initiatingView.id]?.toCharArray()?.get(1)!!)
        return arrayOf(a, b)
    }

    fun exchangeIfNull(possibleMove: String, current: String, userCall: Boolean = false) {
        val f: Int = Character.getNumericValue(possibleMove[0])
        val s: Int = Character.getNumericValue(possibleMove[1])

        if (f > 3 || f < 0 || s < 0 || s > 3) {
            return
        }

        if (drawableStructures[possibleMove]?.drawable == null) {
            var aa = drawableStructures[current]!!.currentPosition
            var bb = drawableStructures[possibleMove]!!.currentPosition!!

            drawableStructures[possibleMove]?.drawable = drawableStructures[current]?.drawable;
            drawableStructures[possibleMove]?.currentPosition = aa;

            drawableStructures[current]?.drawable = null
            drawableStructures[current]?.currentPosition = bb

            sync(userCall)
        }
    }


    fun sync(userCall: Boolean = false) {
        for (i in 0..3) {
            for (j in 0..3) {
                imageviews["$i$j"]?.invalidate()
                imageviews["$i$j"]?.setImageDrawable(drawableStructures["$i$j"]?.drawable)
                if (drawableStructures["$i$j"]?.drawable == null) {
                    emptyTile = "$i$j"
                }
            }
        }

        // CHECK
        var correct: Boolean = true;
        for (i in 0..3) {
            for (j in 0..3) {
                if (!drawableStructures["$i$j"]?.initialPosition
                        .equals(drawableStructures["$i$j"]?.currentPosition)
                ) {
                    correct = false
                }
            }
        }

        if (correct) {
            layout.container.background = resources.getDrawable(R.drawable.back, theme)
            if (userCall) {
                score++
                layout.score.text = "".plus(score)
            }
        } else {
            layout.container.background = resources.getDrawable(R.drawable.wrong, theme)
        }

    }
}