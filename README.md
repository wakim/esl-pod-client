# ESL Pod Client

This is a hobby and unofficial project to learn some concepts and tools, including:

 - Kotlin language
 - MVP pattern with RxJava (not RxKotlin)
 - Dagger2
 
The app uses jsoup to gather the podcasts of ESL Podcast website and allowing the user to store and listen any podcast with streaming or using local storage.

It uses a combination of MediaPlayer and MediaPlayback for streaming audio and DownloadManager to handle the download complexity.

## Some Screenshots

<img src="art/home.jpg" width="180" height="320" alt="Home" />
<img src="art/player_collapsed.jpg" width="180" height="320" alt="Media Player Collapsed" />
<img src="art/player_full_screen.jpg" width="180" height="320" alt="Medial Player FullScreen" />
<img src="art/media_playback.jpg" width="180" height="320" alt="Media Playback with Notifications" />

#### Design made by [Tatipiment](https://github.com/Tatipiment)

## TODO

 - Find a way to proxy the MediaPlayer with a local webserver (NanoHTTPD) and store the content of the podcast while it is being played.
  - The implementation may be hard because the user can seek to any position of audio. So the proxy must handle gaps in the downloaded file and must be able to request correctly.
 
 - Store parsed podcasts from web on local database.
 - Sync folder and update database.

----

## Dependencies

To run this project you need to have:

 - JDK 7
 - Android Studio 2.0 or higher
 - Kotlin plugin for Android Studio

---

## Setup the project

1. Install the dependencies above
2. `$ git clone https://github.com/wakim/esl-pod-client.git` - Clone the project
3. `$ cd esl-pod-client` - Go into the project folder
4. Open Android Studio
5. Click "Import project (Eclipse ADT, Gradle, etc.)"
6. Build the project to see if everything is working fine

## License
<pre>
Copyright 2016 Wakim Jraige

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</pre>
