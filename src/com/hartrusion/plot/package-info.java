/*
 * The MIT License
 *
 * Copyright 2025 Viktor Alexander Hartung.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
/**
 * A very rudimentary plot tool aiming to be compatible to the matlab/octave
 * syntax. There is no support for 3D axes, the corresponding z values and
 * elements do not exist at all.
 * <p>
 * The package aims to provide two things: Classes that can be used in awt and
 * swing GUIs and a very easy and convenient way to just fire off a plot command
 * in any part of any code.
 * <p>
 * GUIs typically place a FigureJPanel (swing) or even FigurePanel (awt), create
 * an Axes object and Line object, add the Axes to the FigurePanel and add the
 * Line to the Axes object.
 * <p>
 * When using the package for fast plots just to visualize while coding (this is
 * the way matlab is used many times), you make a static import of the
 * VisualizeData class in your code and simply put plot(x,y) anywhere.
 */
package com.hartrusion.plot;
