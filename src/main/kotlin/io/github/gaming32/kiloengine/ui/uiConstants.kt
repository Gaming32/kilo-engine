@file:Suppress("MemberVisibilityCanBePrivate", "unused", "SpellCheckingInspection")
@file:JvmName("UIConstants")

package io.github.gaming32.kiloengine.ui

import io.github.gaming32.kiloengine.util.toNVGColor
import java.awt.Color

const val ENGINE_NAME = "Kilo Engine"

const val FORMAT                = '§'

const val FORMAT_REGULAR        = 'r'
const val FORMAT_BOLD           = 'l'
const val FORMAT_ITALICS        = 'i'
const val FORMAT_ITALICS_BOLD   = 'o'

const val FORMAT_BLACK          = '0'
const val FORMAT_DARK_BLUE      = '1'
const val FORMAT_DARK_GREEN     = '2'
const val FORMAT_DARK_AQUA      = '3'
const val FORMAT_DARK_RED       = '4'
const val FORMAT_DARK_PURPLE    = '5'
const val FORMAT_GOLD           = '6'
const val FORMAT_GRAY           = '7'
const val FORMAT_DARK_GRAY      = '8'
const val FORMAT_BLUE           = '9'
const val FORMAT_GREEN          = 'a'
const val FORMAT_AQUA           = 'b'
const val FORMAT_RED            = 'c'
const val FORMAT_PURPLE         = 'd'
const val FORMAT_YELLOW         = 'e'
const val FORMAT_WHITE          = 'f'

const val FORMAT_DEFAULT_TEXT_COLOR = FORMAT_WHITE

const val JETBRAINS_MONO_REGULAR        = "JetBrainsMonoNL-Regular"
const val JETBRAINS_MONO_BOLD           = "JetBrainsMonoNL-ExtraBold"
const val JETBRAINS_MONO_ITALIC         = "JetBrainsMonoNL-Italic"
const val JETBRAINS_MONO_ITALIC_BOLD    = "JetBrainsMonoNL-ExtraBoldItalic"

@JvmField
val JETBRAINS_MONO                      = Font(JETBRAINS_MONO_REGULAR, JETBRAINS_MONO_BOLD, JETBRAINS_MONO_ITALIC, JETBRAINS_MONO_ITALIC_BOLD)

@JvmField
val DEFAULT_FONT_FAMILY                 = JETBRAINS_MONO

const val DEFAULT_FONT                  = JETBRAINS_MONO_REGULAR

const val DEFAULT_TEXT_SIZE             = 11 * 1.5f
const val DEFAULT_TITLE_SIZE            = 22f
const val DEFAULT_PARGRAPH_TEXT_OFFSET  = 20f

@JvmField val BLACK         = Color(0x000000).toNVGColor()
@JvmField val DARK_BLUE     = Color(0x0000AA).toNVGColor()
@JvmField val DARK_GREEN    = Color(0x00AA00).toNVGColor()
@JvmField val DARK_AQUA     = Color(0x00AAAA).toNVGColor()
@JvmField val DARK_RED      = Color(0xAA0000).toNVGColor()
@JvmField val DARK_PURPLE   = Color(0xAA00AA).toNVGColor()
@JvmField val GOLD          = Color(0xFFAA00).toNVGColor()
@JvmField val GRAY          = Color(0xAAAAAA).toNVGColor()
@JvmField val DARK_GRAY     = Color(0x555555).toNVGColor()
@JvmField val BLUE          = Color(0x5555FF).toNVGColor()
@JvmField val GREEN         = Color(0x55FF55).toNVGColor()
@JvmField val AQUA          = Color(0x55FFFF).toNVGColor()
@JvmField val RED           = Color(0xFF5555).toNVGColor()
@JvmField val PURPLE        = Color(0xFF55FF).toNVGColor()
@JvmField val YELLOW        = Color(0xFFFF55).toNVGColor()
@JvmField val WHITE         = Color(0xFFFFFF).toNVGColor()

@JvmField val DEFAULT_TEXT_COLOR = WHITE