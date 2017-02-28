/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.color;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.greip.common.Util;

/**
 * The <code>ColorButton</code> is a button that displays a color and/or text
 * and allows the user to change the color selection.
 *
 * @see Button
 */
public class ColorButton extends Button {

	private RGB rgb;
	private Image image;
	private IColorChooserFactory factory;
	private Consumer<RGB> consumer;

	/**
	 * Constructs a new instance of this class given its parent.
	 *
	 * @param parent
	 *           a composite control which will be the parent of the new instance
	 *           (cannot be null)
	 *
	 * @exception IllegalArgumentException
	 *               <ul>
	 *               <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *               </ul>
	 * @exception SWTException
	 *               <ul>
	 *               <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *               thread that created the parent</li>
	 *               </ul>
	 */
	public ColorButton(final Composite parent) {
		super(parent, SWT.PUSH);

		addListener(SWT.Resize, e -> changeImage());
		addListener(SWT.Dispose, e -> disposeImage());

		addListener(SWT.Selection, e -> {
			if (factory != null && consumer != null) {
				Util.whenNotNull(chooseRGB(factory), consumer);
			}
		});

		setRGB(new RGB(255, 255, 255));
	}

	@Override
	protected void checkSubclass() {
		// allow subclassing
	}

	/**
	 * Opens the color chooser and allows the user to select a color.
	 *
	 * @param factory
	 *           The color chooser factory.
	 *
	 * @return The selected color or null, when the user cancels the dialog.
	 *
	 * @see IColorChooserFactory
	 */
	public RGB chooseRGB(final IColorChooserFactory factory) {
		final ColorChooserPopup colorChooserPopup = new ColorChooserPopup(this);

		colorChooserPopup.createContent(factory);
		colorChooserPopup.setRGB(rgb);
		colorChooserPopup.open();

		final RGB newRGB = colorChooserPopup.getRGB();
		Util.whenNotNull(newRGB, this::setRGB);

		return newRGB;
	}

	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		final Point size = super.computeSize(wHint, hHint, changed);
		return new Point(Math.max(wHint, getTextSize().x + 30), size.y);
	}

	/**
	 * Sets the RGB value for the color and update the control.
	 *
	 * @param rgb
	 *           RGB value
	 */
	public void setRGB(final RGB rgb) {
		this.rgb = rgb;
		changeImage();
	}

	/**
	 * Gets the selected RGB.
	 *
	 * @return RGB
	 */
	public RGB getRGB() {
		return rgb;
	}

	@Override
	public void setText(final String string) {
		super.setText(string);
		changeImage();
	}

	private Point getTextSize() {
		final Point size = Util.getTextSize(this, getText(), SWT.DRAW_MNEMONIC);

		if (size.x > 0) {
			size.x += 6;
		}

		return size;
	}

	private void changeImage() {
		final Point size = getSize();

		if (size.x == 0 || size.y == 0) {
			return;
		}

		final int height = size.y - 16;
		final int width = getText().isEmpty() ? size.x - 21 : height;

		if (height <= 0 || width <= 0) {
			return;
		}

		final PaletteData palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
		final ImageData source = new ImageData(width, height, 24, palette);
		source.transparentPixel = 0;

		disposeImage();
		image = new Image(getDisplay(), source);
		final GC gc = new GC(image);

		if (rgb != null && isEnabled()) {
			final Color color = new Color(gc.getDevice(), rgb);
			gc.setBackground(color);
			gc.fillRectangle(0, 0, width, height);
			color.dispose();
		}

		gc.setForeground(getDisplay().getSystemColor(isEnabled() ? SWT.COLOR_DARK_GRAY : SWT.COLOR_GRAY));
		gc.drawRectangle(0, 0, width - 1, height - 1);
		gc.dispose();

		setImage(image);
	}

	private void disposeImage() {
		Util.whenNotNull(image, image::dispose);
	}

	/**
	 * Sets the factory for creating the color chooser.
	 *
	 * @param factory
	 *           The factory instance.
	 *
	 * @see IColorChooserFactory
	 */
	public void setColorChooserFactory(final IColorChooserFactory factory) {
		this.factory = factory;
	}

	/**
	 * Sets the consumer for the selected color.
	 *
	 * @param consumer
	 *           The color consumer.
	 */
	public void setColorConsumer(final Consumer<RGB> consumer) {
		this.consumer = consumer;
	}
}
