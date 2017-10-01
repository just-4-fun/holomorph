package just4fun.holomorph

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.shouldEqual



class TestTemplate: Spek() { init {
	given("A") {
		on("b") {
			it("c") { shouldEqual(0, 0) }
		}
	}
}
}
