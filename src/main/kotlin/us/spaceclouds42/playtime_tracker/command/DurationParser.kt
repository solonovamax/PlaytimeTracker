package us.spaceclouds42.playtime_tracker.command

import cloud.commandframework.arguments.parser.ArgumentParseResult
import cloud.commandframework.arguments.parser.ArgumentParser
import cloud.commandframework.captions.Caption
import cloud.commandframework.captions.CaptionVariable
import cloud.commandframework.context.CommandContext
import cloud.commandframework.exceptions.parsing.NoInputProvidedException
import cloud.commandframework.exceptions.parsing.ParserException
import java.time.Duration
import java.util.Queue
import java.util.stream.Collectors
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class DurationParser<C : Any> : ArgumentParser<C, Duration> {
    override fun parse(commandContext: CommandContext<C>, inputQueue: Queue<String>): ArgumentParseResult<Duration> {
        val input = inputQueue.peek()
                    ?: return ArgumentParseResult.failure(NoInputProvidedException(
                            DurationParseException::class.java,
                            commandContext
                                                                                  ))
        if (!DURATION_PATTERN.containsMatchIn(input)) {
            return ArgumentParseResult.failure(DurationParseException(input, commandContext))
        }
        
        val matches = DURATION_PATTERN.findAll(input)
        
        var duration = 0.nanoseconds
        
        for (match in matches) {
            // val group: String = matcher.group()
            val group = match.groupValues.first()
            val timeUnit = group[group.length - 1].toString()
            val timeValue = group.substring(0, group.length - 1).toInt()
            duration += when (timeUnit) {
                "d"  -> timeValue.days
                "h"  -> timeValue.hours
                "m"  -> timeValue.minutes
                "s"  -> timeValue.seconds
                else -> return ArgumentParseResult.failure(DurationParseException(input, commandContext))
            }
        }
        
        inputQueue.remove()
        return ArgumentParseResult.success(duration.toJavaDuration())
    }
    
    override fun suggestions(
        commandContext: CommandContext<C>,
        input: String
                            ): List<String> {
        val chars = input.lowercase().toCharArray()
        if (chars.isEmpty()) {
            return IntStream.range(1, 10).boxed()
                    .sorted()
                    .map { obj: Int? -> java.lang.String.valueOf(obj) }
                    .collect(Collectors.toList())
        }
        val last = chars[chars.size - 1]
        
        // 1d_, 5d4m_, etc
        return if (Character.isLetter(last)) {
            emptyList()
        } else Stream.of("d", "h", "m", "s")
                .filter { unit: String? -> !input.contains(unit!!) }
                .map { unit: String -> input + unit }
                .collect(Collectors.toList())
        
        // 1d5_, 5d4m2_, etc
    }
    
    companion object {
        private val DURATION_PATTERN = Regex("(([1-9][0-9]+|[1-9])[dhms])")
    }
    
    class DurationParseException(
        input: String,
        context: CommandContext<*>
                                ) : ParserException(
            DurationParser::class.java,
            context,
            Caption.of("'{input}' is not a duration format"),
            CaptionVariable.of("input", input)
                                                   ) {
        
        companion object {
            private const val serialVersionUID = 7632293268451349508L
        }
    }
    
}
