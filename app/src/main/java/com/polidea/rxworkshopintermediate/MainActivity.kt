package com.polidea.rxworkshopintermediate

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private var disposable: Disposable? = null
    private lateinit var timerTextView: TextView
    private lateinit var scoreTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        timerTextView = findViewById(R.id.timerTv) as TextView
        scoreTextView = findViewById(R.id.scoreTv) as TextView
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
        disposable = null
    }

    private fun updateTime(seconds: Int) {
        timerTextView.text = "$seconds"
    }

    private fun newRandomQuestionAndAnswer(): Pair<String, String> {
        val random = Random()
        val firstInt = random.nextInt(6)
        val secondInt = random.nextInt(5)
        val result = firstInt + secondInt
        return Pair("$firstInt + $secondInt = ?", result.toString())
    }

    class IncorrectAnswerException : RuntimeException()

    sealed class Answer {
        data class CorrectAnswer(val points: Long) : Answer()
        object IncorrectAnswer : Answer()
    }

    data class QuizState(val score: Long) {

        fun add(change: Long): QuizState = QuizState(score + change)

        fun decrement(): QuizState = QuizState(score - 1)
    }
}
