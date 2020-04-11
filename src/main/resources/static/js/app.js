var syncRequest = null;
var playbackData = null;
var player = null;

var opts = {
  lines: 5, // The number of lines to draw
  length: 38, // The length of each line
  width: 17, // The line thickness
  radius: 45, // The radius of the inner circle
  scale: 1, // Scales overall size of the spinner
  corners: 1, // Corner roundness (0..1)
  color: '#ffffff', // CSS color or array of colors
  fadeColor: 'transparent', // CSS color or array of colors
  speed: 1, // Rounds per second
  rotate: 0, // The rotation offset
  animation: 'spinner-line-fade-quick', // The CSS animation name for the lines
  direction: 1, // 1: clockwise, -1: counterclockwise
  zIndex: 2e9, // The z-index (defaults to 2000000000)
  className: 'spinner', // The CSS class to assign to the spinner
  top: '50%', // Top position relative to parent
  left: '50%', // Left position relative to parent
  shadow: '0 0 1px transparent', // Box-shadow for the lines
  position: 'absolute' // Element positioning
};

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
  }
}

function sync() {
  var spinner = new Spinner(opts).spin($("#loading_circle").get());
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
  setTimeout(function(){spinner.stop()}, 1500);
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

  var d = new Date();
  $("#sync_timestamp").html(d.toLocaleTimeString());
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