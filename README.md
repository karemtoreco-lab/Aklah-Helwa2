<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/7ced96fa-30a5-43d1-80f5-6e20b7b589e7

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.


## Build a free APK in your browser (GitHub Actions)

This project ZIP includes `.github/workflows/build-apk.yml`. To build an installable debug APK without installing Android Studio:

1. Create a **private** repository on GitHub.
2. Extract this ZIP on your computer. Upload the **contents** of the extracted folder to the repository (make sure `.github/workflows/build-apk.yml` is included).
3. On GitHub, open the repository's **Actions** tab and enable Actions if prompted.
4. Select **Build Akleh Helwa APK** → **Run workflow**.
5. Wait for the run to finish successfully. Open the completed run and download the `Aklah-Helwa-APK` artifact.
6. Extract the downloaded artifact ZIP; inside it is `app-debug.apk`. You can rename it to `Aklah-Helwa.apk` and send it through WhatsApp.

Notes:
- This produces a **debug-signed APK** for direct installation and testing, not a Google Play release bundle.
- Keep the repository private. Do not upload API keys, `.env` files, passwords, or signing keys.
- If the app uses Gemini API features, configure a valid key securely before relying on those features; never hard-code a private API key in the app.
- For future builds, push changes to `main`/`master` or run the workflow manually again.
