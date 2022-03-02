package com.juancpinillar.justpi.ui

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.GridLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.juancpinillar.justpi.R
import com.juancpinillar.justpi.getDigitDrawableId
import com.juancpinillar.justpi.getDigitId
import com.juancpinillar.justpi.model.PiGame
import com.juancpinillar.justpi.ui.adapters.DigitAdapter
import com.juancpinillar.justpi.ui.adapters.GamePageAdapter
import com.juancpinillar.justpi.ui.settings.SettingsActivity

import kotlinx.android.synthetic.main.activity_main.gameViewPager
import kotlinx.android.synthetic.main.page_play.cueView
import kotlinx.android.synthetic.main.page_digits.currentDigitTextView
import kotlinx.android.synthetic.main.page_digits.digitsRecyclerView

import kotlinx.android.synthetic.main.menu_item.view.menuItemView
import kotlinx.android.synthetic.main.play_digit_content.view.digitContentImageView

class MainActivity : AppCompatActivity(), DigitAdapter.OnDigitLongClickListener {

    companion object {
        private const val KEY_CURRENT_POSITION = "key_current_position"
    }

//    Model
    private lateinit var piGame: PiGame

    private var isHoldingCorrectGuess = false

//    Cached preferences
//    Default values defined in preferences.xml
    private var showCueOnWrongGuess = false
    private var showDigits = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setDefaultPreferences()
        setupGame(savedInstanceState)

        setupViewPager()
        cachePreferences()
    }

//    Setup

    private fun setDefaultPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
    }

    private fun cachePreferences() {
        showDigits = getPersistedShowDigits()
    }

    private fun setupGame(savedInstanceState: Bundle?) {
        val digits: String = getString(R.string.digits)
        piGame = PiGame(digits, getPersistedStartPosition())
        if (savedInstanceState != null) {
            piGame.position = savedInstanceState.getInt(KEY_CURRENT_POSITION)
        }
    }

    private fun setupViewPager() {
        gameViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            private var isOnPlayPage = true

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING && isOnPlayPage && isHoldingCorrectGuess) {
                    isHoldingCorrectGuess = false
                    undoCorrectGuess()
                }
            }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                isOnPlayPage = position == 0
            }
        })
        gameViewPager.adapter = GamePageAdapter(this)
        gameViewPager.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        gameViewPager.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        setupViewPagerViews()
                    }
                }
        )
    }

    private fun setupViewPagerViews() {
        setupPlayPage()
        setupDigitsPage()
    }

    private fun setupPlayPage() {
        setupDigitContainers()
        updateDigitsVisibility()
    }

    private fun setupDigitContainers() {
        for (digit in 0..9) {
            val digitContainer: View = getDigitContainer(digit)
            digitContainer.setOnTouchListener(::onDigitTouch)
            with (digitContainer.digitContentImageView) {
                setImageResource(getDigitDrawableId(this@MainActivity, digit.toString()))
                setColorFilter(Color.WHITE)
            }
        }
    }

    private fun setupDigitsPage() {
        setupDigitsRecyclerView()
        updateDigitCountAndScrollPosition()
    }

    private fun setupDigitsRecyclerView() {
        digitsRecyclerView.apply {
            setHasFixedSize(true)
            val digitsPerRow: Int = resources.getInteger(R.integer.digits_per_row)
            layoutManager = GridLayoutManager(this@MainActivity, digitsPerRow)
            adapter = DigitAdapter(this@MainActivity, piGame, this@MainActivity)
        }
    }

    private inline fun applyOverAllDigits(action: (View) -> Unit) {
        for (digit in 0..9) {
            action(getDigitContainer(digit))
        }
    }

    private fun getDigitContainer(digit: Int): View {
        val viewId = getDigitId(this, digit.toString())
        return findViewById(viewId)
    }

    private fun vibrate() {
        val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val duration = 50L
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    private fun updateDigitsPageOnCorrectGuess() {
        updateRecyclerViewOnCorrectGuess()
        updateDigitCountAndScrollPosition()
    }

    private fun undoCorrectGuess() {
        piGame.position--
        updateRecyclerViewOnUndoneCorrectGuess()
        updateDigitCountAndScrollPosition()
    }

    private fun updateRecyclerViewOnUndoneCorrectGuess() {
        val position: Int = piGame.position
        with(digitsRecyclerView.adapter) {
            notifyItemChanged(position)
            notifyItemChanged(position + 1)
        }
    }

    private fun updateRecyclerViewOnCorrectGuess() {
        val position: Int = piGame.position
        with(digitsRecyclerView.adapter) {
            if (piGame.hasDigitsAvailable) {
                notifyItemChanged(position)
            }
            notifyItemChanged(position - 1)
        }
    }

    private fun updateScrollPosition() {
        val position: Int = piGame.position
        val digitsPerRow: Int = resources.getInteger(R.integer.digits_per_row)
        val rowsAbove: Int = resources.getInteger(R.integer.rows_above_current_row)
        val scrollToTop: Boolean = position < (rowsAbove + 1) * digitsPerRow
        val scrollPosition: Int = if (!scrollToTop) position - rowsAbove * digitsPerRow else 0
        (digitsRecyclerView.layoutManager as GridLayoutManager).scrollToPositionWithOffset(scrollPosition, 0)
    }

    private fun restart() {
        val previousPosition: Int = piGame.position
        piGame.restart()
        updateRecyclerViewOnRestart(previousPosition)
    }

    private fun updateRecyclerViewOnRestart(previousPosition: Int) {
        var lowPosition: Int = previousPosition
        var highPosition: Int = piGame.startPosition
        if (lowPosition > highPosition) {
            lowPosition = piGame.startPosition
            highPosition = previousPosition
        }
        val digitsPageUpdate: () -> Unit = {
            digitsRecyclerView.adapter.notifyItemRangeChanged(lowPosition, highPosition - lowPosition + 1)
            updateDigitCountAndScrollPosition()
        }
        if (gameViewPager.currentItem == 0) {
            if (previousPosition == piGame.numberOfDigits) {
                animatePlayDigitsOnRestart()
                animateCueOnDigitsOver(false, digitsPageUpdate)
            } else {
                animatePlayDigitsOnRestart(digitsPageUpdate)
            }
        } else {
            digitsPageUpdate()
            if (previousPosition == piGame.numberOfDigits) {
                animateCueOnDigitsOver(false)
            }
        }
    }

    private fun changeStartPosition(startPosition: Int) {
        piGame.startPosition = startPosition
        persistNewStartPosition()
        restart()
    }

    private fun updateDigitCountAndScrollPosition() {
        updateDigitCount()
        updateScrollPosition()
    }

    private fun updateDigitCount() {
        currentDigitTextView.text = if (piGame.position == 0) "" else piGame.position.toString()
        val textColorId: Int = if (piGame.position != piGame.startPosition) R.color.colorPositive else R.color.colorSecondary
        currentDigitTextView.setTextColor(ContextCompat.getColor(this, textColorId))
    }

    private fun updateDigitsVisibility() {
        applyOverAllDigits { digitContainer: View ->
            digitContainer.digitContentImageView.visibility = if (showDigits) View.VISIBLE else View.INVISIBLE
        }
    }

//    Animations

    private fun animatePlayDigitsOnRestart(endAction: () -> Unit = {}) {
        val animators: List<Animator> = List(10) { position: Int ->
            createPlayDigitOnRestartAnimator(position)
        }
        AnimatorSet().apply {
            playTogether(animators)
            duration = 220
            withEndAction(endAction)
            start()
        }
    }

    private fun createPlayDigitOnRestartAnimator(digit: Int): Animator {
        val digitContainer: View = getDigitContainer(digit)
        val digitScaleAnimator: Animator = createDigitScaleOnRestartAnimator(digitContainer)
        val digitAlphaAnimator: Animator = createDigitAlphaOnRestartAnimator(digitContainer)
        return AnimatorSet().apply {
            playTogether(digitScaleAnimator, digitAlphaAnimator)
        }
    }

    private fun createDigitScaleOnRestartAnimator(view: View): Animator {
        val shrinkScale = 0.75f
        val scaleShrinkAnimator: Animator = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat(View.SCALE_X, shrinkScale),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, shrinkScale))

        val scaleExpandAnimator = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f))

        return AnimatorSet().apply {
            playSequentially(scaleShrinkAnimator, scaleExpandAnimator)
        }
    }

    private fun createDigitAlphaOnRestartAnimator(view: View): Animator {
        val alphaOutAnimator: Animator = ObjectAnimator.ofFloat(view, View.ALPHA, 0.5f)
        val alphaInAnimator: Animator = ObjectAnimator.ofFloat(view, View.ALPHA, 1f)
        return AnimatorSet().apply {
            playSequentially(alphaOutAnimator, alphaInAnimator)
        }
    }

    private fun animateCueOnWrongGuess() {
        val alphaInAnimator = ObjectAnimator.ofFloat(cueView, View.ALPHA, 0.5f)
        val startScale = 0.6f
        val scaleInAnimator = ObjectAnimator.ofPropertyValuesHolder(cueView,
                PropertyValuesHolder.ofFloat(View.SCALE_X, startScale, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, startScale, 1f))
        val inAnimator = AnimatorSet().apply {
            playTogether(alphaInAnimator, scaleInAnimator)
            interpolator = AccelerateInterpolator()
        }

        val alphaOutAnimator = ObjectAnimator.ofFloat(cueView, View.ALPHA, 0f)
        val endScale = 1.4f
        val scaleOutAnimator = ObjectAnimator.ofPropertyValuesHolder(cueView,
                PropertyValuesHolder.ofFloat(View.SCALE_X, endScale),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, endScale))
        val outAnimator = AnimatorSet().apply {
            playTogether(alphaOutAnimator, scaleOutAnimator)
            interpolator = DecelerateInterpolator()
        }
        val animation = AnimatorSet()
        animation.apply {
            withEndAction {
                cueView.apply {
                    scaleX = 1f
                    scaleY = 1f
                }
            }
            playSequentially(inAnimator, outAnimator)
            duration = 175
            start()
        }
    }

    private fun animateCueOnDigitsOver(digitsOver: Boolean, endAction: () -> Unit = {}) {
        val fromValue = if (digitsOver) 0f else 1f
        val toValue = 1 - fromValue
        val alphaProperty = PropertyValuesHolder.ofFloat(View.ALPHA, fromValue, toValue)
        val scaleXProperty = PropertyValuesHolder.ofFloat(View.SCALE_X, fromValue, toValue)
        val scaleYProperty = PropertyValuesHolder.ofFloat(View.SCALE_Y, fromValue, toValue)

        val animation = ObjectAnimator.ofPropertyValuesHolder(cueView, alphaProperty, scaleXProperty, scaleYProperty)
        if (digitsOver) {
            animation.withStartAction {
                cueView.background.setColorFilter(ContextCompat.getColor(this, R.color.colorPositive), PorterDuff.Mode.SRC_ATOP)
            }
        } else {
            animation.withEndAction {
                cueView.background.setColorFilter(ContextCompat.getColor(this, R.color.colorNegative), PorterDuff.Mode.SRC_ATOP)
                cueView.apply {
                    scaleX = 1f
                    scaleY = 1f
                }
                endAction()
            }
        }
        animation.apply {
            duration = 400
            start()
        }
    }

//    Event listeners

    private fun onDigitTouch(digitContainer: View, event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (piGame.hasDigitsAvailable) {
                    onPlayDigitSelection(digitContainer)
                }
                true
            }
            MotionEvent.ACTION_UP -> {
                if (isHoldingCorrectGuess) {
                    isHoldingCorrectGuess = false
                }
                true
            }
            else -> false
        }
    }

    override fun onDigitLongClick(position: Int) {
        changeStartPosition(position)
    }

    private fun onPlayDigitSelection(digitContainer: View) {
        val digit = digitContainer.tag as String
        val correctGuess: Boolean = piGame.guessDigit(digit)
        if (correctGuess) {
            onCorrectGuess()
        } else {
            onWrongGuess()
        }
    }

    private fun onCorrectGuess() {
        isHoldingCorrectGuess = true
        if (!piGame.hasDigitsAvailable) {
            animateCueOnDigitsOver(true)
        }
        updateDigitsPageOnCorrectGuess()
    }

    private fun onWrongGuess() {
        updateScrollPosition()
        vibrate()
        if (showCueOnWrongGuess && resources.getBoolean(R.bool.is_portrait)) {
            animateCueOnWrongGuess()
        }
    }

//    Preferences

    private fun persistNewStartPosition() {
        val startPosition: Int = piGame.startPosition
        val startingDigit: Int = startPosition + 1
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        with(sharedPreferences.edit()) {
            putInt(getString(R.string.key_starting_digit_pref), startingDigit)
            apply()
        }
    }

    private fun checkForPreferencesChange() {
        val startPosition: Int = getPersistedStartPosition()
        if (startPosition != piGame.startPosition) {
            onPersistedStartPositionChange(startPosition)
        }
        val showDigits: Boolean = getPersistedShowDigits()
        if (this.showDigits != showDigits) {
            onPersistedShowDigitsChange(showDigits)
        }
        val showCueOnWrongGuess: Boolean = getPersistedShowCueOnWrongGuess()
        if (this.showCueOnWrongGuess != showCueOnWrongGuess) {
            onPersistedShowCueOnWrongGuessChange(showCueOnWrongGuess)
        }
    }

    private fun onPersistedShowDigitsChange(showDigits: Boolean) {
        this.showDigits = showDigits
        updateDigitsVisibility()
    }

    private fun onPersistedStartPositionChange(startPosition: Int) {
        piGame.startPosition = startPosition
        restart()
    }

    private fun onPersistedShowCueOnWrongGuessChange(showCueOnWrongGuess: Boolean) {
        this.showCueOnWrongGuess = showCueOnWrongGuess
    }

    private fun getPersistedShowDigits(): Boolean {
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPreferences.getBoolean(getString(R.string.key_show_digits_pref), false)
    }

    private fun getPersistedShowCueOnWrongGuess(): Boolean {
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPreferences.getBoolean(getString(R.string.key_cue_wrong_guess_pref), false)
    }

    private fun getPersistedStartPosition(): Int {
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val startingDigit: Int = sharedPreferences.getInt(getString(R.string.key_starting_digit_pref), -1)
        return startingDigit - 1
    }

//    Options menu

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)

        val restartItem: MenuItem = menu.findItem(R.id.restart_item)
        restartItem.setActionView(R.layout.menu_item)
        with (restartItem.actionView.menuItemView) {
            setImageResource(R.drawable.restart_icon)
            setOnClickListener { onRestartItemClick() }
            setOnLongClickListener { onRestartItemLongClick(); true }
        }
        val settingsItem: MenuItem = menu.findItem(R.id.settings_item)
        settingsItem.setActionView(R.layout.menu_item)
        with (settingsItem.actionView.menuItemView) {
            setImageResource(R.drawable.settings_icon)
            setOnClickListener { onSettingsItemClick() }
        }
        return true
    }

    private fun onSettingsItemClick() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun onRestartItemLongClick() {
        changeStartPosition(0)
        vibrate()
    }

    private fun onRestartItemClick() {
        restart()
    }

//    Activity lifecycle methods

    override fun onResume() {
        checkForPreferencesChange()
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_POSITION, piGame.position)
    }

//    Extension functions

    private fun Animator.withStartAction(startAction: () -> Unit) {
        this.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {
                startAction()
            }
        })
    }

    private fun Animator.withEndAction(endAction: () -> Unit) {
        this.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                endAction()
            }
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
        })
    }
}
