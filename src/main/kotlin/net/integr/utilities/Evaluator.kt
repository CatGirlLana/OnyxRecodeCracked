/*
 * Copyright © 2024 Integr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.integr.utilities

import kotlin.math.pow


class Evaluator {
    companion object {
        fun evaluate(form: String): String {
            var copy = replaceConstants(form)
            while (copy.contains('(')) {
                val innerBracketContent = evalBrackets(copy)
                val calculatedBracketContent  = evalInternal(innerBracketContent)
                val toReplace = "($innerBracketContent)"

                copy = copy.replace(toReplace, calculatedBracketContent)
            }

            val evaluatedContent = evalInternal(copy)
            return evaluatedContent
        }

        private fun evalBrackets(form: String): String { // accepts whole ops: "x*(x*(5-5))"
            val count = form.count {c -> c == '(' }
            if (count == form.count {c -> c == ')' }) {
                if (count > 0) {
                    val lIndex = form.lastIndexOf('(')
                    return form.substring((lIndex+1)..<findClosingBracket(form, lIndex))
                } else {
                    return form
                }
            } else throw IllegalArgumentException("Bracket error!")
        }

        private fun findClosingBracket(form: String, index: Int): Int {
            for (i in (index..form.lastIndex)) {
                if (form[i] == ')') {
                    return i
                }
            }

            throw IllegalArgumentException("Bracket error!")
        }

        private fun replaceConstants(form: String): String {
            var copy = form
            copy = copy.replace("e", Math.E.toString())
            copy = copy.replace("pi", Math.PI.toString())
            return copy
        }

        private fun evalTimes(form: String): String { // accepts single ops: "56*53"
            val index = form.indexOf('*')
            val first = form.substring(0..<index)
            val last = form.substring((index+1)..<form.length)

            return (first.toDouble() * last.toDouble()).toString()
        }

        private fun evalDiv(form: String): String { // accepts single ops: "56/53"
            val index = form.indexOf('/')
            val first = form.substring(0..<index)
            val last = form.substring((index+1)..<form.length)

            return (first.toDouble() / last.toDouble()).toString()
        }

        private fun evalPlus(form: String): String { // accepts single ops: "56+53"
            val index = form.indexOf('+')
            val first = form.substring(0..<index)
            val last = form.substring((index+1)..<form.length)

            return (first.toDouble() + last.toDouble()).toString()
        }

        private fun evalMinus(form: String): String { // accepts single ops: "56-53"
            val index = form.indexOf('-')
            val first = form.substring(0..<index)
            val last = form.substring((index+1)..<form.length)

            return (first.toDouble() - last.toDouble()).toString()
        }

        private fun evalPow(form: String): String { // accepts single ops: "56^3"
            val index: Int = form.indexOf('^')
            val first = form.substring(0..<index)
            val last = form.substring((index+1)..<form.length)

            return first.toDouble().pow(last.toDouble()).toString()
        }

        private fun isDot(c: Char): Boolean {
            return c == '/' || c == '*'
        }

        private fun isLine(c: Char): Boolean {
            return c == '-' || c == '+'
        }

        private fun isPow(c: Char): Boolean {
            return c == '^'
        }

        private fun isOperation(c: Char): Boolean {
            return isDot(c) || isLine(c) || isPow(c)
        }

        private fun containsValidMinus(form: String): Boolean {
            if (!form.contains('-')) return false
            for (i in (0..form.lastIndex)) {
                if (form[i] == '-') {
                    if (i != 0) {
                        return true
                    }
                }
            }
            return false
        }

        private fun evalInternal(form: String): String { // accepts ops: "x-5*5/5" // cant handle negative nums
            val p1 = replacePowers(form)
            val p2 = replaceDots(p1)
            return replaceLines(p2)
        }

        private fun replaceDots(form: String): String { // accepts ops: "x-5*5/5"
            var copy = form

            while (copy.contains('*') || copy.contains('/')) {
                copy = replaceOneDot(copy)
            }

            return copy
        }

        private fun replaceLines(form: String): String { // accepts ops: "x-5+5"
            var copy = form

            while (copy.contains('+') || containsValidMinus(copy)) {
                copy = replaceOneLine(copy)
            }

            return copy
        }

        private fun replacePowers(form: String): String { // accepts ops: "x-5^5"
            var copy = form

            while (copy.contains('^')) {
                copy = replaceOnePow(copy)
            }

            return copy
        }

        private fun replaceOneDot(form: String): String { // accepts ops: "x-5*5/5"
            for (i in (0..form.lastIndex)) {
                if (isDot(form[i])) {
                    val op = getOperationAround(i, form)
                    if (form[i] == '*') {
                        val res = evalTimes(op)
                        return form.replace(op, res)
                    } else {
                        val res = evalDiv(op)
                        return form.replace(op, res)
                    }
                }
            }

            return form
        }

        private fun replaceOneLine(form: String): String { // accepts ops: "x-5+5"
            for (i in (0..form.lastIndex)) {
                if (isLine(form[i])) {
                    val op = getOperationAround(i, form)
                    if (form[i] == '+') {
                        val res = evalPlus(op)
                        return form.replace(op, res)
                    } else if (i != 0) {
                        val res = evalMinus(op)
                        return form.replace(op, res)
                    }
                }
            }

            return form
        }

        private fun replaceOnePow(form: String): String { // accepts ops: "x-5+5"
            for (i in (0..form.lastIndex)) {
                if (isPow(form[i])) {
                    val op = getOperationAround(i, form)
                    val res = evalPow(op)
                    return form.replace(op, res)

                }
            }

            return form
        }

        private fun getOperationAround(index: Int, form: String): String { // accepts ops: "x-5*5/5"
            var start = index
            var end = index

            var seenNumberEnd = false

            for (i in (index-1 downTo 0)) {
                if (isOperation(form[i])) {
                    start = i+1
                    break
                }
            }

            if (start == index) start = 0

            for (i in (index+1..form.lastIndex)) {
                if (form[i].isDigit()) seenNumberEnd = true
                if (isOperation(form[i]) && seenNumberEnd) {
                    end = i
                    break
                }
            }

            if (end == index) end = form.length

            return form.substring(start..<end)
        }
    }
}