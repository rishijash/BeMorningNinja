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

// Helpers
var currentUsername = "";
var fallbackVideoUrl = "";

document.addEventListener("DOMContentLoaded", function () {

    // Selectors
    var wrapper = document.getElementById('wrapper');
    var alarmHours = document.getElementById('alarm_hours');
    var alarmMinutes = document.getElementById('alarm_minutes');
    var alarmAMPM = document.getElementById('alarm_ampm');
    var submit = document.getElementById('submit');
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
    function Alarm(hourArg, minArg, nameArg, fallbackVideoArg) {
        this.hourArg = hourArg;
        this.minArg = minArg;
        this.name = nameArg;
        this.fallbackVideo = fallbackVideoArg;

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
            var newAlarm = new Alarm(hour, minute, currentUsername, fallbackVideoUrl);
            alarmObj = newAlarm;

            // Update alarm message text
            alarmMessage.innerHTML = "All set! @" + currentUsername + " will wake you up!";

            submit.innerText = "CANCEL";

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
            // Play it
            var alarmPlayerBody = document.getElementById("alarmPlayerBody");
            alarmPlayerBody.innerHTML = "<center><video id=\"alarmVideo\" width=\"320\" height=\"240\" controls autoplay loop src=\"" + url + "\">\n" +
                "                                Your browser does not support the video tag.\n" +
                "                            </video></center>";
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

function quoteAction(alarmPlayerBody,           happyMorning) {
    var alarmPlayerTitle = document.getElementById("alarmPlayerTitle");
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
                    alarmPlayerTitle.innerText = "Not enough inspired.. Let's change that! 😉";
                    $("#alarmPlayerFooter").hide();
                    var quote = quotes[0].quote;
                    alarmPlayerBody.innerHTML = "<br><h5><i>\"" + quote + "\"</i></h5><br><br>Quotes by: <a hre='http://quotes.rest'>Quotes.rest</a>";
                    setTimeout(function() {
                        $("#alarmPlayer").modal('close');
                    }, 10000);
                } else {
                    $("#alarmPlayer").modal('close');
                }
            },
            error: function (jqXHR, status, err) {
                alarmPlayerBody.innerHTML = "";
                $("#alarmPlayer").modal('close');
            }
        });
    } else {
        alarmPlayerBody.innerHTML = "";
        $("#alarmPlayer").modal('close');
    }

}

// Account Constructor
function Account(username, gym, sleepy, picture, summary, selectedVideo, genre) {
    this.username = username;
    this.gym = gym;
    this.sleepy = sleepy;
    this.picture = picture;
    this.summary = summary;
    this.selectedVideo = selectedVideo;
    this.genre = genre;
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
    if(!fromAPI) {
        jsonObj = JSON.parse(result);
    }
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
        var genre = profile.account.genre;
        var accountObj = new Account(username, gymCount, sleepCount, picture, summary, selectedVideo, genre);
        accounts.push(accountObj);
    }
    return accounts;
}

function renderAccountList(accounts, slider) {
    // Render accounts list
    for (i in accounts) {
        var account = accounts[i];
        var htmlChild = "<div class=\"carousel-item\" data-cindex=\"" + i + "\">\n" +
            "                            <div style=\"position: relative;\">\n" +
            "                                <img src=\"" + account.picture + "\" height=\"100%\" width=\"100%\">\n" +
            "                                <div class=\"img-card\" style=\" \">\n" +
            "                                    <h6><a target='_blank' style='color: white; text-decoration: underline; font-size: 18px;' href='https://instagram.com/" + account.username + "'>@" + account.username + "</a><span>&nbsp&nbsp 💪" + account.gym + " 😴" + account.sleepy + "</span></h6>\n" +
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
            alarmMessage.innerHTML = "Influencer Selected: @" + currentUsername;
            fallbackVideoUrl = accounts[cindex].selectedVideo;
        }
    });
}

$(document).ready(function(){
    $('.modal').modal();

    var slider = $('.carousel.carousel-slider');
    var loader = $('.loaderDiv');
    var accounts = [];
    var testData = "{ \"profiles\": [ { \"username\": \"garyvee\", \"account\": { \"dataId\": \"FRwJjR9dMyGIHUT8dUUq\", \"username\": \"garyvee\", \"accountPicture\": \"https://upload.wikimedia.org/wikipedia/commons/c/cd/Gary_Vaynerchuk_public_domain.jpg\", \"accountSummary\": \"\", \"thumbsupCount\": 0, \"sleepyCount\": 1, \"gymCount\": 11 }, \"profileUrl\": \"https://www.instagram.com/garyvee\", \"summary\": \"🔮 Investor Facebook, Snap, Venmo & Uber\\n✈️ CEO of @vaynermedia & @vaynersports \\n💻 Check @one37pm a hot new site\\n👇🏼Subscribe to my 🔥 podcast 🎧\", \"profilePic\": \"https://scontent-iad3-1.cdninstagram.com/vp/9bcda296b036828fe20aa349e28a85c6/5E23E12B/t51.2885-19/s150x150/49409756_359167334882615_1833195288651628544_n.jpg?_nc_ht=scontent-iad3-1.cdninstagram.com\", \"selectedVideoUrl\": { \"displayUrl\": \"https://scontent-iad3-1.cdninstagram.com/vp/86d365be266ba3d477d12fc3bc493858/5D89087E/t51.2885-15/fr/e15/p1080x1080/69293201_180362269792297_8075937881308947572_n.jpg?_nc_ht=scontent-iad3-1.cdninstagram.com&_nc_cat=1\", \"instagramPostUrl\": \"https://www.instagram.com/p/B2sFB9KABcw/\", \"videoLink\": \"https://scontent.cdninstagram.com/v/t50.16885-16/10000000_1139611109761293_289744403519233370_n.mp4?_nc_ht=scontent.cdninstagram.com&_nc_cat=109&oe=5D8A01EC&oh=0ea09426c25d0f9365767057b46df8c4\" }, \"selectedImageUrl\": { \"displayUrl\": \"https://scontent-iad3-1.cdninstagram.com/vp/bd25405fa284bffe3e2f75753b9d2c80/5E3C5CB5/t51.2885-15/e35/71528099_733403820495191_4309738476731828091_n.jpg?_nc_ht=scontent-iad3-1.cdninstagram.com&_nc_cat=1\", \"instagramPostUrl\": \"https://www.instagram.com/p/B2ru9ZWAObP/\" } }, { \"username\": \"cernyfit\", \"account\": { \"dataId\": \"0tMT6sb65ppPHIg5yGYY\", \"username\": \"cernyfit\", \"accountPicture\": \"https://scontent-lax3-1.cdninstagram.com/vp/1c609250501c551083678799172c7e70/5E3D5502/t51.2885-19/s150x150/69039356_650775845410148_7096071385071484928_n.jpg?_nc_ht=scontent-lax3-1.cdninstagram.com\", \"accountSummary\": \"\" }, \"profileUrl\": \"https://www.instagram.com/cernyfit\", \"summary\": \"Main account: @AmandaCerny. \\nWelcome to my fitness account!\\nYour daily dose of wellness🍃💪🏼\", \"profilePic\": \"https://scontent-iad3-1.cdninstagram.com/vp/bd4e3d8a37e02dff8e3120ae74d4f335/5E3D5502/t51.2885-19/s150x150/69039356_650775845410148_7096071385071484928_n.jpg?_nc_ht=scontent-iad3-1.cdninstagram.com\", \"selectedVideoUrl\": { \"displayUrl\": \"https://scontent-iad3-1.cdninstagram.com/vp/a915298f62a73be04e21b1dbf044cfbb/5D89313A/t51.2885-15/e35/69279473_505063943616572_7653102409577006742_n.jpg?_nc_ht=scontent-iad3-1.cdninstagram.com&_nc_cat=108\", \"instagramPostUrl\": \"https://www.instagram.com/p/B11jgytDwIT/\", \"videoLink\": \"https://scontent.cdninstagram.com/v/t50.16885-16/70043418_439691413303345_1446127276244990573_n.mp4?_nc_ht=scontent.cdninstagram.com&_nc_cat=101&oe=5D88E234&oh=a0425a5e2168989f30500295f9889f3b\" }, \"selectedImageUrl\": { \"displayUrl\": \"https://scontent-iad3-1.cdninstagram.com/vp/0091b07f32514be16eb957e80257aab1/5E39E0E7/t51.2885-15/e35/p1080x1080/69790204_2353868377995647_5558603780867183345_n.jpg?_nc_ht=scontent-iad3-1.cdninstagram.com&_nc_cat=111\", \"instagramPostUrl\": \"https://www.instagram.com/p/B2Zbs4Vjpqc/\" } }, { \"username\": \"thebodycoach\", \"account\": { \"dataId\": \"px8UedS383hU1dVls4QB\", \"username\": \"thebodycoach\", \"accountPicture\": \"https://i.guim.co.uk/img/media/a11bfd2db9429f52e52e24639af1d447fb94da36/559_368_3898_4872/master/3898.jpg?width=300&quality=85&auto=format&fit=max&s=56ecb2b0dbe15bd01214fa9632b1b55e\", \"accountSummary\": \"\", \"thumbsupCount\": 0, \"sleepyCount\": 1, \"gymCount\": 1 }, \"profileUrl\": \"https://www.instagram.com/thebodycoach\", \"summary\": \"On a mission to inspire one new person everyday to exercise and cook a healthy meal 😊\", \"profilePic\": \"https://scontent-iad3-1.cdninstagram.com/vp/3ebdadec43ff20003c68dd962e232e89/5E3787DD/t51.2885-19/s150x150/42669825_2216497948631670_5697409766096109568_n.jpg?_nc_ht=scontent-iad3-1.cdninstagram.com\", \"selectedVideoUrl\": { \"displayUrl\": \"https://scontent-iad3-1.cdninstagram.com/vp/0c09cdca878489d3d7d13794be98467c/5D8922C0/t51.2885-15/e35/70414116_682034702304144_7521045283030899041_n.jpg?_nc_ht=scontent-iad3-1.cdninstagram.com&_nc_cat=100\", \"instagramPostUrl\": \"https://www.instagram.com/p/B2e88IvHPNd/\", \"videoLink\": \"https://scontent.cdninstagram.com/v/t50.2886-16/71302561_2359896127661091_3182429968931246536_n.mp4?_nc_ht=scontent.cdninstagram.com&_nc_cat=103&oe=5D895DD4&oh=e3de29d0778ed719452a9ed8890e383f\" }, \"selectedImageUrl\": { \"displayUrl\": \"https://scontent-iad3-1.cdninstagram.com/vp/dbbd348bc5edaa457ee62b6df22758a7/5E22687B/t51.2885-15/e35/69875611_749864195452647_8773034366963558104_n.jpg?_nc_ht=scontent-iad3-1.cdninstagram.com&_nc_cat=1\", \"instagramPostUrl\": \"https://www.instagram.com/p/B2sFpvRnknw/\" } }, { \"username\": \"nofrontspodcast\", \"account\": { \"dataId\": \"ndhjHbbVSohZasORuldC\", \"username\": \"nofrontspodcast\", \"accountPicture\": \"https://scontent-lax3-1.cdninstagram.com/vp/a9c284dadeac05c5bdb44f59bcce2c3e/5DFABB06/t51.2885-19/s320x320/57939603_491601198044044_5725603537744822272_n.jpg?_nc_ht=scontent-lax3-1.cdninstagram.com\", \"accountSummary\": \"\", \"thumbsupCount\": 0, \"sleepyCount\": 3, \"gymCount\": 1 }, \"profileUrl\": \"https://www.instagram.com/nofrontspodcast\", \"summary\": \"🚗| One Year Ago I Lived In My Car\\n🎙| # 5 Business Podcast on ITunes \\n🧔🏻| Founder of @socialauthentic \\n 👇🏼NEWEST Episode No Fronts Sept 12👇🏼\", \"profilePic\": \"https://scontent-iad3-1.cdninstagram.com/vp/8e2c46c6012532f5dd06db5e3d72d7b4/5E1EFBF6/t51.2885-19/s150x150/57939603_491601198044044_5725603537744822272_n.jpg?_nc_ht=scontent-iad3-1.cdninstagram.com\", \"selectedVideoUrl\": { \"displayUrl\": \"https://scontent-iad3-1.cdninstagram.com/vp/5869ff211871804d5e9d057964f0921f/5D89721F/t51.2885-15/e35/69599881_155839938809859_5827654462882447979_n.jpg?_nc_ht=scontent-iad3-1.cdninstagram.com&_nc_cat=103\", \"instagramPostUrl\": \"https://www.instagram.com/p/B2r69JPAvwA/\", \"videoLink\": \"https://scontent.cdninstagram.com/v/t50.2886-16/71296275_405014630196131_3154421708694143313_n.mp4?_nc_ht=scontent.cdninstagram.com&_nc_cat=111&oe=5D88CE95&oh=dd6f5fb2411f83e27ca9dea938ab46df\" }, \"selectedImageUrl\": { \"displayUrl\": \"https://scontent-iad3-1.cdninstagram.com/vp/b54adb310adc2c029e30165250832a19/5E053DDE/t51.2885-15/e35/69650304_1368307623335782_8374753560229332755_n.jpg?_nc_ht=scontent-iad3-1.cdninstagram.com&_nc_cat=110\", \"instagramPostUrl\": \"https://www.instagram.com/p/B2qFJABguIG/\" } } ] }";
    var url = serverBaseUrl + "profiles";
    $.ajax({
        url: url,
        type: 'GET',
        success: function(result, status, xhr) {
            accounts = pushAccountData(result, true);
        },
        error: function (jqXHR, status, err) {
            accounts = pushAccountData(testData, false);
            alert("Something went wrong.. Please referesh the page!");
        },
        complete: function (jqXHR, status) {
            loader.hide();
            renderAccountList(accounts, slider);
        }
    });
});