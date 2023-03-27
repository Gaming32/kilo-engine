@file:Suppress("MemberVisibilityCanBePrivate", "unused", "SpellCheckingInspection")
@file:JvmName("UIConstants")

package io.github.gaming32.kiloengine.ui

const val ENGINE_NAME = "Kilo Engine"

const val FORMAT = '§';

const val FORMAT_REGULAR = 'r'
const val FORMAT_BOLD = 'b'
const val FORMAT_ITALICS = 'i'
const val FORMAT_ITALICS_BOLD = 'l'

const val JETBRAINS_MONO_REGULAR = "JetBrainsMonoNL-Regular"
const val JETBRAINS_MONO_BOLD = "JetBrainsMonoNL-ExtraBold"
const val JETBRAINS_MONO_ITALIC = "JetBrainsMonoNL-Italic"
const val JETBRAINS_MONO_ITALIC_BOLD = "JetBrainsMonoNL-ExtraBoldItalic"

@JvmField
val JETBRAINS_MONO = Font(JETBRAINS_MONO_REGULAR, JETBRAINS_MONO_BOLD, JETBRAINS_MONO_ITALIC, JETBRAINS_MONO_ITALIC_BOLD)

@JvmField
val DEFAULT_FONT_FAMILY = JETBRAINS_MONO

const val DEFAULT_FONT = JETBRAINS_MONO_REGULAR

const val DEFAULT_TEXT_SIZE = 11 * 1.5f
const val DEFAULT_TITLE_SIZE = 22f
const val DEFAULT_PARGRAPH_TEXT_OFFSET = 20f
