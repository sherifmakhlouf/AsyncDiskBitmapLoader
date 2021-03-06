package com.lilmak.asyncdiskbitmaploader.util;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

public class FileBitmapWorkerTask extends AsyncTask<String, Void, Bitmap>
		implements BitmapWorkerTask {

	private final WeakReference<ImageView> imageView;
	private String data;
	private BitmapFactory.Options options;

	public FileBitmapWorkerTask(ImageView iv, BitmapFactory.Options op) {
		imageView = new WeakReference<ImageView>(iv);
		if (op != null)
			options = op;

	}

	@Override
	protected Bitmap doInBackground(String... params) {
		data = params[0];
		BitmapFactory.Options options = new Options();
		options.inSampleSize = 1;
		// TODO use default options
		if (this.options != null)
			return BitmapFactory.decodeFile(data, options);
		return decodeSampledBitmapFromResource(data,
				imageView.get().getWidth(), imageView.get().getHeight());

	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {

		if (isCancelled()) {
			bitmap = null;
		}

		if (imageView != null && bitmap != null) {

			final ImageView iv = imageView.get();
			final FileBitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(iv);
			if (this == bitmapWorkerTask && imageView != null) {
				iv.setImageBitmap(bitmap);

			}
		}

	}

	@Override
	public Object getData() {
		return data;
	}

	public static boolean cancelPotentialWork(String path, ImageView imageView) {
		final FileBitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final String bitmapData = (String) bitmapWorkerTask.getData();
			if (bitmapData != path) {
				// Cancel previous task
				bitmapWorkerTask.cancel(true);
			} else {
				// The same work is already in progress
				return false;
			}
		}
		// No task associated with the ImageView, or an existing task was
		// cancelled
		return true;

	}

	private static FileBitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return (FileBitmapWorkerTask) asyncDrawable
						.getBitmapWorkerTask();
			}
		}
		return null;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(String path,
			int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// BitmapFactory.decodeResource(res, resId, options);
		BitmapFactory.decodeFile(path, options);
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

}