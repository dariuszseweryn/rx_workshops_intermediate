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
//        disposable = Observable.merge<Boolean>(
//                countdownCompletable(10).andThen(Observable.just(false)),
//                awaitCode("111").andThen(Observable.just(true))
//        )
//                .take(1)
//                .subscribe {
//                    timerTextView.text = if(it) "SUCCESS!" else "FAILURE!"
//                }
//        disposable = countdownCompletable(5)
//                .observeOn(Schedulers.computation())
//                .repeatWhen {
//                    it.flatMap {
//                        codeDialogObservable().flatMap { when(it) {
//                            "111" -> Observable.just(true)
//                            "999" -> Observable.just(false)
//                            else -> Observable.empty()
//                        } }
//                                .take(1)
//                                .toFlowable(BackpressureStrategy.LATEST)
//                    }
//                            .takeWhile { it }
//                }
//                .subscribe()

        disposable = Observable.combineLatest(
                questionUser()
                        .andThen(Observable.just<Answer>(Answer.CorrectAnswer(1)))
                        .onErrorReturn { Answer.IncorrectAnswer },
                Observable.interval(1, TimeUnit.MILLISECONDS),
                BiFunction { answer: Answer, time: Long ->
                    Log.d("Answer", "after $time $answer")
                    return@BiFunction when(answer) {
                        is Answer.CorrectAnswer -> Answer.CorrectAnswer(TimeUnit.SECONDS.toMillis(10) - time)
                        MainActivity.Answer.IncorrectAnswer -> answer
                    }
                }
        )
                .take(1)
                .take(3, TimeUnit.SECONDS)
                .repeat()
                .takeUntil(countdownCompletable(30).toObservable<Answer>())
                .scan(QuizState(0), { quizState, quizEvent ->
                    when (quizEvent) {
                        is MainActivity.Answer.CorrectAnswer -> quizState.add(quizEvent.points)
                        MainActivity.Answer.IncorrectAnswer -> quizState.decrement()
                    }
                })
//                .lastOrError()
//                .toList()
//                .map {
//                    var score: Long = 0
//                    it.forEach {
//                        if (it is Answer.CorrectAnswer) {
//                            score += it.points
//                        } else {
//                            score -= 1
//                        }
//                    }
//                    QuizState(score)
//                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        Consumer { scoreTextView.text = "${it.score}" }
                )

        codeDialogObservable("dsadsa").repeatWhen { it.flatMap { countdownCompletable(10).andThen(Observable.just(1)) } }
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
        disposable = null
    }

    private fun updateTime(seconds: Int) {
        timerTextView.text = "$seconds"
    }

    private fun questionUser() = Observable.fromCallable { newRandomQuestionAndAnswer() }
            .flatMap { (question, answer) -> codeDialogObservable(question).map { it == answer } }
            .firstOrError()
            .flatMapCompletable { if (it) Completable.complete() else Completable.error(IncorrectAnswerException()) }

    private fun codeDialogObservable(message: String): Observable<String> = Observable.create<String> { emitter ->
        val codeInputDialog = CodeInputDialog(this@MainActivity, message = message) {
            Log.d("Code", it)
            emitter.onNext(it)
        }
        codeInputDialog.show()
        emitter.setCancellable { codeInputDialog.dismiss() }
    }
            .subscribeOn(AndroidSchedulers.mainThread())
            .unsubscribeOn(AndroidSchedulers.mainThread())

    private fun countdownCompletable(seconds: Int) = Observable.interval(0, 1, TimeUnit.SECONDS, Schedulers.computation())
            .map { (seconds - it).toInt() }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { updateTime(it) }
            .takeUntil { it == 0 }
            .ignoreElements()

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
