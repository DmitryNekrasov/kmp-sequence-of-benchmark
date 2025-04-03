package singlesequenceof

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Blackhole
import kotlinx.benchmark.Measurement
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Param
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import kotlin.random.Random

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 5, time = 1)
open class SequenceOperationsBenchmark {

    // Basic creation benchmarks
    @Benchmark
    fun sequenceOfCreationDefault(blackhole: Blackhole, state: CreationState) {
        repeat(state.count) {
            val seq = sequenceOf(1)
            blackhole.consume(seq)
        }
    }

    @Benchmark
    fun sequenceOfCreationSingle(blackhole: Blackhole, state: CreationState) {
        repeat(state.count) {
            val seq = singleSequenceOf(1)
            blackhole.consume(seq)
        }
    }

    // Terminal operation benchmarks
    @Benchmark
    fun sequenceFirst(blackhole: Blackhole, state: SequenceState) {
        var result = state.sequence.first()
        blackhole.consume(result)
    }

    // Chain of transformations
    @Benchmark
    fun sequenceChain(blackhole: Blackhole, state: SequenceState) {
        val result = state.sequence
            .map { it * 3 }
            .filter { (it and 1) == 1 }
            .firstOrNull()
        blackhole.consume(result)
    }

    // Real-world scenario
    @Benchmark
    fun sequenceRealWorld(blackhole: Blackhole, state: SequenceState) {
        val baseValue = 78
        val result = state.sequence
            .map { it + baseValue }
            .map { it * 3 }
            .filter { (it and 1) == 1 }
            .map { it.toString() }
            .map { it.length }
            .sum()
        blackhole.consume(result)
    }

    // Polymorphic call site benchmark
    @Benchmark
    fun polymorphicCallSite(blackhole: Blackhole, state: PolymorphicState) {
        var sum = 0
        for (seq in state.sequences) {
            sum += seq
                .map { it * 2 }
                .filter { it > 0 }
                .filter { it % 2 == 0 }
                .firstOrNull() ?: 0
        }
        blackhole.consume(sum)
    }

    @State(Scope.Benchmark)
    class CreationState {
        @Param("1", "10", "1000", "1000000")
        var count: Int = 0
    }

    @State(Scope.Benchmark)
    class SequenceState {
        @Param("default", "single")
        private lateinit var type: String

        lateinit var sequence: Sequence<Int>

        @Setup
        fun setup() {
            val element = RANDOM.nextInt(0, 1_000_000) * 2 + 1
            sequence = when (type) {
                "default" -> sequenceOf(element)
                "single" -> singleSequenceOf(element)
                else -> throw IllegalArgumentException("Unknown sequence type: $type")
            }
        }
    }

    @State(Scope.Benchmark)
    class PolymorphicState {
        @Param("default_only", "single_only", "mixed")
        private lateinit var scenario: String

        private val elements = List(100) { RANDOM.nextInt(0, 1_000_000) * 2 + 1 }
        lateinit var sequences: List<Sequence<Int>>

        @Setup
        fun setup() {
            sequences = when (scenario) {
                "default_only" -> elements.map { sequenceOf(it) }
                "single_only" -> elements.map { singleSequenceOf(it) }
                "mixed" -> elements.mapIndexed { index, value ->
                    if (index % 2 == 0) sequenceOf(value) else singleSequenceOf(value)
                }
                else -> throw IllegalArgumentException("Unknown scenario: $scenario")
            }
        }
    }

    companion object {
        val RANDOM = Random(0xcafebabe)
    }
}