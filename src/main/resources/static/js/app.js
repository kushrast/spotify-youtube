var syncRequest = null;
var playbackData = null;
var player = null;

function onLoad() {
    $("body").css("background","rgb(255,221,225)");
    $("body").css("background","linear-gradient(90deg, rgba(255,221,225,1) 0%, rgba(238,156,167,1) 100%)");
  $("#authenticated").hide();
  var refresh_token = Cookies.get("refresh_token");
  if (refresh_token != null && refresh_token != "") {
    $("body").css("background","");
    $("body").css("background-color","black");
    $( "#not_authenticated" ).remove();
    $( "#authenticated" ).show();
    $("#loading_circle").hide();
  }
}

function sync() {
  $("#loading_circle").show();
  $("#sync_timestamp").html("Syncing");
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

  var d = new Date();

  setTimeout(function() {
    $("#loading_circle").hide();
    $("#sync_timestamp").html("Last synced: " + d.toLocaleTimeString());
  }, 1500);
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
    $("#sync").html("Stop syncing");
    sync();
    syncRequest = setInterval(sync, 15000);
  }
}

$("#sync").on("click", toggleSync);

onLoad();