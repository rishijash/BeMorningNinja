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

document.addEventListener("DOMContentLoaded", function () {

    // Helpers
    var currentUsername = "";

    // Selectors
    var wrapper = document.getElementById('wrapper');
    var alarmHours = document.getElementById('alarm_hours');
    var alarmMinutes = document.getElementById('alarm_minutes');
    var alarmName = document.getElementById('alarm_name');
    var alarmAMPM = document.getElementById('alarm_ampm');
    var submit = document.getElementById('submit');
    var alarmMessage = document.getElementById('alarm_message');
    var alarmList = document.getElementById('alarm_list');

    var triggered = false;

    // Create array to store alarm times in
    var alarms = [];

    // Create alarm elements
    createAlarmElements();

    // Render initial time, then update every second after
    renderTime();
    setInterval(renderTime, 1000);

    // Set event listener for adding alarms
    addListenerForInput();

    // Alarm constructor
    function Alarm(hourArg, minArg, nameArg) {
        this.hourArg = hourArg;
        this.minArg = minArg;
        this.name = nameArg;

        this.getHour = function () {
            return this.hourArg;
        };

        this.getMinute = function () {
            return this.minArg;
        };

        this.getName = function () {
            return this.name;
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

        if (timeMinutes < 10) {
            timeMinutesDisplay = '0' + timeMinutes;
        }

        if (timeSeconds < 10) {
            timeSecondsDisplay = '0' + timeSeconds;
        }

        // Display time
        wrapper.innerHTML = timeHoursDisplay + ':' + timeMinutesDisplay + ':' + timeSecondsDisplay + ' ' + ampm;

        // Check if time matches alarm time

        if (alarms.length > 0) {
            for (var i = 0; i < alarms.length; i++) {
                var checkAlarm = alarms[i];
                if (checkAlarm.getHour() == timeHours && checkAlarm.getMinute() == timeMinutes) {
                    ringAlarm(alarms[i]);
                }
            }
        }


    }


    // Add a listener to the alarm input
    function addListenerForInput() {
        submit.addEventListener('click', setAlarm);
    }

    // Set alarm
    function setAlarm() {

        if (alarmName.value && alarmHours.value && alarmMinutes.value && alarmAMPM.value) {

            var hour = 0;
            var minute = 0;

            if (alarmAMPM.value == "PM") {
                hour = parseInt(alarmHours.value) + 12;
            } else {
                hour = parseInt(alarmHours.value);
            }

            minute = parseInt(alarmMinutes.value);

            // Create new alarm object and store it in alarms[]
            var newAlarm = new Alarm(hour, minute, alarmName.value);
            alarms.push(newAlarm);

            // Update alarm message text
            alarmMessage.innerHTML = "Your alarm";

            // Append date to list of dates in DOM
            var newListItem = document.createElement('li');
            newListItem.innerHTML = alarmName.value + " - " + alarmHours.value + ":" + alarmMinutes.value + " " + alarmAMPM.value;
            alarmList.appendChild(newListItem);
        } else {
            alert('ERROR: valid time and name needed to set an alarm');
        }
    }

    // Do something when the alarm goes off
    function ringAlarm(alarmData) {
        // get Instagram Video File for Username
        if (!triggered) {
            // get Video Link
            currentUsername = alarmData.getName();
            var url = getVideoLinkFromInstagram(alarmData.getName());
            // Play it
            var alarmPlayerBody = document.getElementById("alarmPlayerBody");
            alarmPlayerBody.innerHTML = "<center><video id=\"alarmVideo\" width=\"320\" height=\"240\" controls autoplay loop src=\"" + url + "\">\n" +
                "                                Your browser does not support the video tag.\n" +
                "                            </video></center>";
            $("#alarmPlayer").modal();
            triggered = true;
        }
    }

    $('#strongMorning').click(function(){
        //Some code
        var alarmPlayerBody = document.getElementById("alarmPlayerBody");
        var url = serverBaseUrl + "accounts/" + currentUsername + "?gym=true";
        $.ajax({
            url: url,
            type: 'PUT',
            success: function(result) {
            }
        });
        quoteAction(alarmPlayerBody, true);
    });

    $('#lazyMorning').click(function(){
        //Some code
        var alarmPlayerBody = document.getElementById("alarmPlayerBody");
        var url = serverBaseUrl + "accounts/" + currentUsername + "?sleepy=true";
        $.ajax({
            url: url,
            type: 'PUT',
            success: function(result) {
            }
        });
        quoteAction(alarmPlayerBody, false);
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
                    alarmPlayerBody.innerHTML = "<br><h3><i>\"" + quote + "\"</i></h3><br><br>Quotes by: <a hre='http://quotes.rest'>Quotes.rest</a>";
                    setTimeout(function() {
                        $("#alarmPlayer").modal('toggle');
                    }, 10000);
                } else {
                    $("#alarmPlayer").modal('toggle');
                }
            },
            error: function (jqXHR, status, err) {
                alarmPlayerBody.innerHTML = "";
                $("#alarmPlayer").modal('toggle');
            }
        });
    } else {
        alarmPlayerBody.innerHTML = "";
        $("#alarmPlayer").modal('toggle');
    }

}

// Account Constructor
function Account(username, gym, sleepy, picture, summary) {
    this.username = username;
    this.gym = gym;
    this.sleepy = sleepy;
    this.picture = picture;
    this.summary = summary;
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

$(document).ready(function(){
    var accounts = [];
    var url = serverBaseUrl + "accounts";
    $.ajax({
        url: url,
        type: 'GET',
        success: function(result, status, xhr) {
            var jsonObj = JSON.parse(JSON.stringify(result));
            var jsonArr = jsonObj.accounts;
            for (i in jsonArr) {
                var account = jsonArr[i];
                var username = account.username;
                var maybeGymCount = account.gymCount;
                var maybeSleepyCount = account.sleepyCount;
                var gymCount = 0;
                var sleepCount = 0;
                if(maybeGymCount != null && maybeGymCount != "undefined") gymCount = parseInt(maybeGymCount);
                if(maybeSleepyCount != null && maybeSleepyCount != "undefined") sleepCount = parseInt(maybeSleepyCount);
                var picture = account.accountPicture;
                var summary = account.accountSummary;
                var accountObj = new Account(username, gymCount, sleepCount, picture, summary);
                accounts.push(accountObj);
            }
        },
        error: function (jqXHR, status, err) {
            accounts.push(new Account("garyvee", 0, 0, "", ""))
        },
        complete: function (jqXHR, status) {
            // Render accounts list
            var nameList = document.getElementById('alarm_name');
            for (i in accounts) {
                var opt = document.createElement('option');
                var textValue = accounts[i].username + " Gym: " + accounts[i].gym + " Sleepy: " + accounts[i].sleepy + " Picture: " + accounts[i].picture + " Summary: " + accounts[i].summary;
                opt.value = accounts[i].username;
                opt.innerHTML = textValue;
                nameList.appendChild(opt);
            }
        }
    });
});