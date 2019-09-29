/*
Author: Rushi Jash
Date: 09/02/2019
Notes:
    - Basic Alarm Clock by Ivan Rocha: https://github.com/theitrain/clock
    - Color palette adapted from color.adobe.com project "The Clock Strikes Nine" by user nicolagilroy ( https://color.adobe.com/The-Clock-Strikes-Nine-color-theme-1294130/ )
    - "Alarm Clock" font from dafont.com, created by David J. Patterson ( http://www.dafont.com/alarm-clock.font )
 */

// Constants
var retryCount = 2;
var serverBaseUrl = "https://bemorningninja.herokuapp.com/";
var quoteBaseUrl = "https://quotes.rest/qod?category=inspire";
var defaultMotivationAudio = "https://d3ctxlq1ktw2nl.cloudfront.net/production/2019-7-13/20842925-44100-2-225949da7b5c3.mp3";
var defaultError = "<center>Oppsss!<br>Looks like we are overwhelmed.<br>Please try again. (Apologies) 😓</center>";

// Helpers
var currentUsername = "";
var fallbackVideoUrl = "";
var fallbackImageUrl = "";

var breakfasts = [];

document.addEventListener("DOMContentLoaded", function () {

    // Selectors
    var wrapper = document.getElementById('wrapper');
    var alarmHours = document.getElementById('alarm_hours');
    var alarmMinutes = document.getElementById('alarm_minutes');
    var alarmAMPM = document.getElementById('alarm_ampm');
    var submit = document.getElementById('submitBtn');
    var alarmMessage = document.getElementById('alarm_message');

    var triggered = false;

    // Create array to store alarm times in
    var alarmObj = null;

    // Create alarm elements
    createAlarmElements();

    // Render initial time, then update every second after
    renderTime();
    setInterval(renderTime, 1000);

    // Set event listener for adding alarm
    addListenerForInput();

    // Alarm constructor
    function Alarm(hourArg, minArg, nameArg, fallbackVideoArg, fallbackImageArg) {
        this.hourArg = hourArg;
        this.minArg = minArg;
        this.name = nameArg;
        this.fallbackVideo = fallbackVideoArg;
        this.fallbackImage = fallbackImageArg;

        this.getHour = function () {
            return this.hourArg;
        };

        this.getMinute = function () {
            return this.minArg;
        };

        this.getName = function () {
            return this.name;
        };

        this.getFallbackVideo = function() {
            return this.fallbackVideo;
        };

        this.getFallbackImage = function() {
            return this.fallbackImage;
        }

    }

    // Functions
    function createAlarmElements() {

        for (i = 1; i <= 12; i++) {
            var hourOption = document.createElement('option');
            hourOption.innerHTML = '<option value="' + i + '">' + i + '</option>';
            alarmHours.appendChild(hourOption);
        }

        for (i = 0; i < 60; i++) {

            // Pad minutes
            var output;
            if (i < 10) {
                output = '0' + i;
            } else {
                output = i;
            }

            var minuteOption = document.createElement('option');
            minuteOption.innerHTML = '<option value="' + output + '">' + output + '</option>';
            alarmMinutes.appendChild(minuteOption);
        }
    }

    // Render time
    function renderTime() {

        // create new date object
        var time = new Date();
        var ampm = 'AM';

        // Get values
        var timeSeconds = time.getSeconds();
        var timeMinutes = time.getMinutes();
        var timeHours = time.getHours();

        var timeHoursDisplay = timeHours;
        var timeMinutesDisplay = timeMinutes;
        var timeSecondsDisplay = timeSeconds;

        // Format values
        if (timeHours > 12) {
            timeHoursDisplay -= 12;
            ampm = 'PM';
        }
        if (timeHours == 0) {
            timeHoursDisplay = 12;
            timeHours = 12;
            ampm = 'AM';
        }
        if (timeHours == 12) {
            ampm = 'PM';
        }

        if (timeMinutes < 10) {
            timeMinutesDisplay = '0' + timeMinutes;
        }

        if (timeSeconds < 10) {
            timeSecondsDisplay = '0' + timeSeconds;
        }

        // Display time
        wrapper.innerHTML = timeHoursDisplay + ':' + timeMinutesDisplay + ':' + timeSecondsDisplay + ' ' + ampm;

        // Check if time matches alarm time

        if (alarmObj != null) {
            var checkAlarm = alarmObj;
            if (checkAlarm.getHour() == timeHours && checkAlarm.getMinute() == timeMinutes) {
                ringAlarm(alarmObj);
            }
        }


    }


    // Add a listener to the alarm input
    function addListenerForInput() {
        submit.addEventListener('click', checkAlarm);
    }

    function checkAlarm() {
        var alarmText = submit.innerText;

        if(alarmText == "SET ALARM") {
            setAlarm();
        } else {
            cancelAlarm();
        }
    }

    // Cancel Alarm
    function cancelAlarm() {
        alarmObj = null;
        alarmMessage.innerHTML = "";

        submit.innerText = "SET ALARM";

        // enable stuff back
        $('#carouselRow').css("pointer-events","auto");
        $("#alarm_hours").prop("disabled", false);
        $("#alarm_minutes").prop("disabled", false);
        $("#alarm_ampm").prop("disabled", false);
        $('select').material_select();
    }

    // Set alarm
    function setAlarm() {
        if (currentUsername.length > 0 && alarmHours.value && alarmMinutes.value && alarmAMPM.value) {

            var hour = 0;
            var minute = 0;

            if (alarmAMPM.value == "PM" && alarmHours.value != 12) {
                hour = parseInt(alarmHours.value) + 12;
            } else {
                hour = parseInt(alarmHours.value);
            }

            minute = parseInt(alarmMinutes.value);

            // Create new alarm object
            var newAlarm = new Alarm(hour, minute, currentUsername, fallbackVideoUrl, fallbackImageUrl);
            alarmObj = newAlarm;

            // Update alarm message text
            alarmMessage.innerHTML = "<b>All set! @" + currentUsername + " will wake you up! 🤘</b>";

            submit.innerText = "CANCEL";

            triggered = false;

            // disable stuff
            $('#carouselRow').css("pointer-events","none");
            $("#alarm_hours").prop("disabled", true);
            $("#alarm_minutes").prop("disabled", true);
            $("#alarm_ampm").prop("disabled", true);
            $('select').material_select();

        } else {
            alert('ERROR: Still loading the influencers..');
        }
    }

    // Do something when the alarm goes off
    function ringAlarm(alarmData) {
        // get Instagram Video File for Username
        if (!triggered) {
            // get Video Link
            currentUsername = alarmData.getName();
            var url = alarmData.getFallbackVideo();
            var imageUrl = alarmData.getFallbackImage();
            // Play it
            var alarmPlayerBody = document.getElementById("alarmPlayerBody");
            if(imageUrl != null && imageUrl.length > 0) {
                // Show Image and Play Audio
                alarmPlayerBody.innerHTML = "<center><audio id=\"alarmVideo\" controls autoplay loop src=\"" + url + "\">\n" +
                    "                                Your browser does not support the video tag.\n" +
                    "                            </audio><br><img src=\"" + imageUrl + "\" height=\"240\" ></center>";

            } else {
                // Play Video
                alarmPlayerBody.innerHTML = "<center><video id=\"alarmVideo\" width=\"320\" height=\"240\" controls autoplay loop src=\"" + url + "\">\n" +
                    "                                Your browser does not support the video tag.\n" +
                    "                            </video></center>";
            }
            $("#alarmPlayer").modal('open');
            triggered = true;
        }
    }

    $('#strongMorning').click(function(){
        var alarmPlayerBody = document.getElementById("alarmPlayerBody");
        var url = serverBaseUrl + "accounts/" + currentUsername + "?gym=true";
        $.ajax({
            url: url,
            type: 'PUT',
            success: function(result) {
            }
        });
        quoteAction(alarmPlayerBody, true);
        cancelAlarm();
    });

    $('#nextCarousel').click(function () {
        $('.carousel.carousel-slider').carousel('next');
    });

    $('#prevCarousel').click(function () {
        $('.carousel.carousel-slider').carousel('prev');
    });


    $('#lazyMorning').click(function(){
        var alarmPlayerBody = document.getElementById("alarmPlayerBody");
        var url = serverBaseUrl + "accounts/" + currentUsername + "?sleepy=true";
        $.ajax({
            url: url,
            type: 'PUT',
            success: function(result) {
            }
        });
        quoteAction(alarmPlayerBody, false);
        cancelAlarm();
    });

});

function quoteAction(alarmPlayerBody, happyMorning) {
    var alarmPlayerTitle = document.getElementById("alarmPlayerTitle");
    var alarmPlayerFooter = document.getElementById("alarmPlayerFooter");
    if(!happyMorning) {
        $.ajax({
            url: quoteBaseUrl,
            type: 'GET',
            headers: {"Accept": "application/json"},
            success: function(result) {
                alarmPlayerBody.innerHTML = "";
                var jsonObj = JSON.parse(JSON.stringify(result));
                var contentsObj = jsonObj.contents;
                var quotes = contentsObj.quotes;
                if(quotes.length > 0) {
                    alarmPlayerTitle.innerText = "Sorry, we don't believe in Snooze button! 😉";
                    var quote = quotes[0].quote;
                    alarmPlayerBody.innerHTML = "<br><center><audio id=\"alarmVideo\" width=\"320\" height=\"240\" controls autoplay loop src=\"" + defaultMotivationAudio + "\">\n" +
                        "                                Your browser does not support the video tag.\n" +
                        "                            </audio></center><br>" +
                    "<br><h5><i>\"" + quote + "\"</i></h5><br>Quotes by: <a href='http://quotes.rest'>Quotes.rest</a> and audio by <a href='https://player.fm'>Player.fm</a>";
                    alarmPlayerFooter.innerHTML = "<div class=\"row\"> <div class=\"col s12 m12\"> <center> <a class=\"waves-effect waves-light btn\" id=\"strongMorningAfterSleepy\" onclick='defaultGreetings()'><span>💪</span> I am pumped up!!!</a> </center> </div> </div><br>";
                } else {
                    defaultGreetings();
                }
            },
            error: function (jqXHR, status, err) {
                defaultGreetings();
            }
        });
    } else {
        defaultGreetings();
    }

}

function defaultGreetings() {
    $("#alarmPlayerFooter").hide();
    var randomBreakfast = breakfasts[Math.floor(Math.random()*breakfasts.length)];
    var alarmPlayerBody = document.getElementById("alarmPlayerBody");
    alarmPlayerBody.innerHTML = "<br><center><h5>Have a fantastic day!!!</h5></center><br><br><br>" +
        "<h6>Our suggestion: Start your day with a healthy meal:" +
        "<a href='" + randomBreakfast.post + "' target='_blank'>" +
        "<img src=\"" + randomBreakfast.postImage + "\" height=\"240\">" +
        "</a></h6>";
    setTimeout(function() {
        $("#alarmPlayer").modal('close');
    }, 10000);
}

// Account Constructor
function Account(username, gym, sleepy, picture, summary, selectedVideo, genre, selectedImage) {
    this.username = username;
    this.gym = gym;
    this.sleepy = sleepy;
    this.picture = picture;
    this.summary = summary;
    this.selectedVideo = selectedVideo;
    this.genre = genre;
    this.selectedImage = selectedImage;
}

// Breakfast Constructor
function Breakfast(username, post, postImage) {
    this.username = username;
    this.post = post;
    this.postImage = postImage;
}

function httpGet(theUrl) {
    var tryCount = 0;
    var res = httpGetCall(theUrl);
    while(tryCount < retryCount && res.status != 200) {
        res = httpGetCall(theUrl)
    }
    return res.responseText;
}

function httpGetCall(url) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open("GET", url, false); // false for synchronous request
    xmlHttp.send(null);
    return new HttpRes(xmlHttp.status, xmlHttp.responseText);
}

// Res Constructor
function HttpRes(status, responseText) {
    this.status = status;
    this.responseText = responseText;
}

function pushAccountData(result, fromAPI) {
    var accounts = [];
    var jsonObj = JSON.parse(JSON.stringify(result));
    var loaderText = $('.loaderText');
    if(!fromAPI) {
        jsonObj = JSON.parse(result);
    }
    if (typeof jsonObj == 'undefined' || jsonObj == null) {
        alert(defaultError);
    } else {
        var jsonArr = jsonObj.profiles;
        for (i in jsonArr) {
            var profile = jsonArr[i];
            var username = profile.username;
            var maybeGymCount = profile.account.gymCount;
            var maybeSleepyCount = profile.account.sleepyCount;
            var gymCount = 0;
            var sleepCount = 0;
            if(maybeGymCount != null && maybeGymCount != "undefined") gymCount = parseInt(maybeGymCount);
            if(maybeSleepyCount != null && maybeSleepyCount != "undefined") sleepCount = parseInt(maybeSleepyCount);
            var picture = profile.account.accountPicture;
            var summary = profile.summary;
            var selectedVideo = profile.selectedVideoUrl.videoLink;
            var selectedImage = profile.selectedImageUrl.displayUrl;
            var genre = profile.account.genre;
            if(genre == 'Breakfast') {
                // Add Breakfast Constructor
                var selectedPost = profile.selectedVideoUrl.instagramPostUrl;
                var postImage = profile.selectedVideoUrl.displayUrl;
                var breakfastObj = new Breakfast(username, selectedPost, postImage);
                breakfasts.push(breakfastObj);
            } else {
                // Add Account
                var accountObj = new Account(username, gymCount, sleepCount, picture, summary, selectedVideo, genre, selectedImage);
                accounts.push(accountObj);
            }
        }
        return accounts;
    }
}

function renderAccountList(accounts, slider) {
    // Render accounts list
    for (i in accounts) {
        var account = accounts[i];
        var htmlChild = "<div class=\"carousel-item\" data-cindex=\"" + i + "\">\n" +
            "                            <div style=\"position: relative;\">\n" +
            "                                <img src=\"" + account.picture + "\" height=\"100%\" width=\"100%\">\n" +
            "                                <div class=\"img-card\" style=\" \">\n" +
            "                                    <h6><a target='_blank' style='color: white; text-decoration: underline; font-size: 18px;' href='https://instagram.com/" + account.username + "'>@" + account.username + "</a><span>&nbsp&nbsp 💪&nbsp <b>" + account.gym + "</b>&nbsp 😴 &nbsp<b>" + account.sleepy + "</b></span></h6>\n" +
            "                                    <h6 style='margin-top: 15px;'>Type: " + account.genre + "</h6>\n" +
            "                                    <span class='summaryText'>" + account.summary + "</span>\n" +
            "                                </div>\n" +
            "                            </div>\n" +
            "                        </div>";
        slider.append(htmlChild);
    }
    slider.carousel({
        fullWidth: false,
        indicators: true,
        onCycleTo: function(data) {
            var cindex = $(data).data('cindex');
            currentUsername = accounts[cindex].username;
            var alarmMessage = document.getElementById('alarm_message');
            alarmMessage.innerHTML = "<b>Influencer Selected: @" + currentUsername + "</b>";
            fallbackVideoUrl = accounts[cindex].selectedVideo;
            fallbackImageUrl = accounts[cindex].selectedImage;
        }
    });
}

$(document).ready(function(){
    $('.modal').modal({
        dismissible: false
    });


    // Hide on mobile
    var mobile = (/iphone|ipod|android|blackberry|mini|windows\sce|palm/i.test(navigator.userAgent.toLowerCase()));
    if (!mobile) {
        $('#mobileOnly').hide();
    }
     else {
        $('#desktopOnly').hide();
    }

    var slider = $('.carousel.carousel-slider');
    var loader = $('.loaderDiv');
    var loaderText = $('.loaderText');
    var accounts = [];
    var url = serverBaseUrl + "profiles";
    $.ajax({
        url: url,
        type: 'GET',
        success: function(result, status, xhr) {
            accounts = pushAccountData(result, true);
            loader.hide();
            renderAccountList(accounts, slider);
        },
        error: function (jqXHR, status, err) {
            loaderText.html(defaultError);
        }
    });
});