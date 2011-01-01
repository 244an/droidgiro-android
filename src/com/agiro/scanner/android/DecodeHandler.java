/*
 * Copyright (C) 2010 ZXing authors
 *
 * Parts of the file was modified by aGiro authors
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

package com.agiro.scanner.android;

import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.PlanarYUVLuminanceSource;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.graphics.Bitmap;
import java.util.HashMap;

import java.util.Hashtable;
import java.lang.Thread;

final class DecodeHandler extends Handler {

  private static final String TAG = DecodeHandler.class.getSimpleName();

  private final CaptureActivity activity;

  DecodeHandler(CaptureActivity activity) {
    this.activity = activity;
  }

  @Override
  public void handleMessage(Message message) {
    switch (message.what) {
      case R.id.decode:
        decode((byte[]) message.obj, message.arg1, message.arg2);
        break;
      case R.id.quit:
        Looper.myLooper().quit();
        break;
    }
  }

  /**
   * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
   * reuse the same reader objects from one decode to the next.
   *
   * @param data   The YUV preview frame.
   * @param width  The width of the preview frame.
   * @param height The height of the preview frame.
   */
  private void decode(byte[] data, int width, int height) {
    long start = System.currentTimeMillis();
    String rawResult = null;
    PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(data, width, height);
    Bitmap bmp = source.renderCroppedGreyscaleBitmap();
    rawResult = BitmapParser.decode(bmp);

    if (rawResult != null) {
      HashMap resultMap = StringDecoder.parseString(rawResult);
      Bitmap debug_bmp = BitmapParser.getDebugBitmap();
      long end = System.currentTimeMillis();
      Log.d(TAG, "Found barcode (" + (end - start) + " ms):\n" + "123");
      Message message = Message.obtain(activity.getHandler(), R.id.decode_succeeded, resultMap);
      Bundle bundle = new Bundle();
      bundle.putParcelable(DecodeThread.BARCODE_BITMAP, debug_bmp);
      message.setData(bundle);
      //Log.d(TAG, "Sending decode succeeded message...");
      message.sendToTarget();
    } else {
      Message message = Message.obtain(activity.getHandler(), R.id.decode_failed);
      message.sendToTarget();
    }
  }

}
