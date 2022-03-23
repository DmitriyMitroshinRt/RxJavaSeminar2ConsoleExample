import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

fun main() {

    fun wash(plate: Int) = Single.timer(400, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.computation())
        .doOnSuccess { println("Plate $plate washed") }
        .map { plate }


    fun dry(plate: Int) = Single.timer(2, TimeUnit.SECONDS)
        .doOnSuccess { println("Plate $plate dried") }
        .map { plate }


    fun queue() = Flowable.create<Int>({ emitter ->

        for (i in 0..100) {
            emitter.onNext(i)
            Thread.sleep(100)
        }

        emitter.onComplete()

    }, BackpressureStrategy.MISSING)



    queue().doOnNext { println("Take a plate $it") }
        .subscribeOn(Schedulers.single())
        .onBackpressureDrop()
        .concatMapSingle(::wash)

        .blockingSubscribe {

            dry(it)
                .blockingSubscribe()
        }
}
