package just4fun.holomorph



fun <T> measureTime(tag: String = "", times: Int = 1, warmup: Boolean = true, antiSurgeRate:Double = .0, code: () -> T): T {
	// warm-up
	var prevTime = 0L
	if (warmup) {
		val ratioMax = 5f / 4f
		var count = 0
		do {
			val t0 = System.nanoTime()
			code()
			val time = System.nanoTime() - t0
			val ratio = prevTime / time.toDouble()
//			if (ratio < 1) println("Warmup $count;  recent= $prevTime;  curr= $time;   ratio= $ratio")
			prevTime = time
		} while (count++ < 2 || ratio > ratioMax)
	}
	//
	var result: T
	var count = times
	var t = 0L
	var t1 = 0L
	do {
		val t0 = System.nanoTime()
		result = code()
		t1 = System.nanoTime() - t0
		if (antiSurgeRate > 0 && prevTime > 0 && t1 >= prevTime*antiSurgeRate) continue // against extreme surges @ by java class newInstance()
		t += t1
		prevTime = t1
		count--
	} while (count > 0)
	println("$tag ::  $times times;  ${t / 1000000} ms;  $t ns;  ${t / times} ns/call")
	totalNs += t
	totalN++
	return result
}

private var totalNs = 0L
private var totalN = 0

val measuredTimeAvg get() = if (totalN == 0) 0 else totalNs / totalN
