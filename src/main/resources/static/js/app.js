var syncRequest = null;
var playbackData = null;
var player = null;

function darkMode() {
  $("body").css("background","");
  $("body").css("background-color","black");
}

function lightMode() {
  $("body").css("background","rgb(255,221,225)");
  $("body").css("background","linear-gradient(90deg, rgba(255,221,225,1) 0%, rgba(238,156,167,1) 100%)");
}

function onLoad() {
  $("#authenticated").hide();
  lightMode();
  var refresh_token = Cookies.get("refresh_token");
  if (refresh_token != null && refresh_token != "") {
    if (typeof(Storage) !== "undefined" && sessionStorage.colorMode) {
      if (sessionStorage.colorMode  != "light") {
        toggleColor();
      }
    } else {
      toggleColor();
    }
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

    var width;
    var height;

    var y = window.outerHeight;
    var x = window.outerWidth;

    if (x/y > 1.59) {
      height = y*.85;
      width = height*16/9
    } else {
      width = x*.98
      height = width*9/16;
    }

    if (width < 200 || height < 200) {
      width = 200;
      height = 200;
    } else if (width > 1920 || height > 1080) {
      width = 1920;
      height = 1080;
    }

    player = new YT.Player('youTubePlayer', {
      height: parseInt(height),
      width: parseInt(width),
      videoId: 'xWggTb45brM',
    });
  }
}

function toggleSync() {
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

function toggleColor() {
  var colorToggle = $("#color_toggle");
  if (typeof(Storage) !== "undefined") {
    sessionStorage.colorMode = colorToggle.val();
  }
  if (colorToggle.val() == "dark") {
    colorToggle.val("light");
    darkMode();
    colorToggle.removeClass("btn-outline-light").addClass("btn-outline-dark");
    colorToggle.html("Enable Light Mode");
  } else {
    colorToggle.val("dark");
    lightMode();
    colorToggle.removeClass("btn-outline-dark").addClass("btn-outline-light");
    colorToggle.html("Enable Dark Mode");
  }
}

$( window ).resize(function() {
    var width;
    var height;

    var y = window.outerHeight;
    var x = window.outerWidth;

    if (x/y > 1.59) {
      height = y*.85;
      width = height*16/9
    } else {
      width = x*.98
      height = width*9/16;
    }

    if (width < 200 || height < 200) {
      width = 200;
      height = 200;
    } else if (width > 1920 || height > 1080) {
      width = 1920;
      height = 1080;
    }

    console.log(height);

    player.setSize(width, height);
});

$("#color_toggle").on('click', toggleColor)

$("#sync").on("click", toggleSync);

onLoad();