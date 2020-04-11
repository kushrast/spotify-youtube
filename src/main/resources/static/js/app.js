var syncRequest = null;
var playbackData = null;
var player = null;

function onLoad() {
  $("#authenticated").hide();
  var refresh_token = Cookies.get("refresh_token");
  if (refresh_token != null && refresh_token != "") {
    $('body').css({background: "" });
    $('body').css({backgroundColor: "black" });
    $( "#not_authenticated" ).remove();
    $( "#authenticated" ).show();
  }
}

function sync() {
  var spotifyUri = playbackData != null ? playbackData["spotifyUri"] : "";
  var oldYouTubeId = playbackData != null ? playbackData["youTubeId"] : "";
  console.log(oldYouTubeId);
  $.getJSON({
    url: "/update",
    data: {
      "spotifyUri": spotifyUri
    }
  })
  .done(function( data ) {
    console.log( "data:", data);
    playbackData = data;

    $("#youtubePlayer").remove();

    var progress = data["progressSeconds"];
    var youTubeId = data["youTubeId"] != null ? data["youTubeId"] : oldYouTubeId;
    var sameVideo = youTubeId == oldYouTubeId;

    playbackData["youTubeId"] = youTubeId;

    if (youTubeId == "") {
      alert("encountered error while getting latest state from server.");
    } else {
      updateYouTubePlayer(sameVideo, youTubeId, progress, data["currentlyPlaying"]);
  }
});
}

function updateYouTubePlayer(isSameVideo, videoId, progress, currentlyPlaying) {
  if (isSameVideo) {
    player.seekTo(progress, true);
  } else {
    player.loadVideoById(videoId, progress);
  }

  console.log("currentlyPlaying: " + currentlyPlaying);
  if (currentlyPlaying) {
        console.log("Play");
    player.playVideo();
  } else {
        console.log("Pause");
    player.pauseVideo();
  }
  player.mute();
}

function onYouTubePlayerAPIReady() {
  var refresh_token = Cookies.get("refresh_token");
  if (refresh_token != null && refresh_token != "") {
    player = new YT.Player('youTubePlayer', {
      height: '720',
      width: '1280',
      videoId: 'xWggTb45brM',
    });
  }
}

function toggleSync(e) {
  if (syncRequest != null) {
    $("#sync").html("Start syncing");
    clearInterval(syncRequest);
    syncRequest = null;
  } else {
    $("#sync").html("Stop sync");
    sync();
    syncRequest = setInterval(sync, 15000);
  }
}

$("#sync").on("click", toggleSync);

onLoad();