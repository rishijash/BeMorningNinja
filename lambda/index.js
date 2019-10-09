/* eslint-disable  func-names */
/* eslint-disable  no-console */

const Alexa = require('ask-sdk-core');
var request = require('sync-request');

const PlayAudioIntentHandler = {
  canHandle(handlerInput) {
    return handlerInput.requestEnvelope.request.type === 'LaunchRequest'
        || (handlerInput.requestEnvelope.request.type === 'IntentRequest'
            && handlerInput.requestEnvelope.request.intent.name === 'PlayAudioIntent');
  },
  handle(handlerInput) {

    return handlerInput.responseBuilder
        .speak("Welcome to Ninja Skill! Say hit me with motivation to pump up yourself!")
        .getResponse();

  },
};

const HelpIntentHandler = {
  canHandle(handlerInput) {
    return handlerInput.requestEnvelope.request.type === 'IntentRequest'
        && handlerInput.requestEnvelope.request.intent.name === 'AMAZON.HelpIntent';
  },
  handle(handlerInput) {
    const speechText = 'This skill plays motivational audios. Say hit me with motivation to get a short motivational audio.';

    return handlerInput.responseBuilder
        .speak(speechText)
        .getResponse();
  },
};

const AboutIntentHandler = {
  canHandle(handlerInput) {
    return handlerInput.requestEnvelope.request.type === 'IntentRequest'
        && handlerInput.requestEnvelope.request.intent.name === 'AboutIntent';
  },
  handle(handlerInput) {
    const speechText = 'Ninja skill plays audios from motivational Instagram Influencer.';

    return handlerInput.responseBuilder
        .speak(speechText)
        .getResponse();
  },
};

const HitMeIntentHandler = {
  canHandle(handlerInput) {
    return handlerInput.requestEnvelope.request.type === 'IntentRequest'
        && (handlerInput.requestEnvelope.request.intent.name === 'HitMeIntent');
  },
  handle(handlerInput) {
    try {
      var res = request('GET', 'https://bemorningninja.herokuapp.com/alexa/profile');
      var body = res.getBody();
      var jsonContent = JSON.parse(body);
      var selectedProfile = jsonContent.profile;
      var selectedUsername = selectedProfile.username;
      var selectedVideoUrl = selectedProfile.selectedVideoUrl.videoLink;

      return handlerInput.responseBuilder
          .speak("Let's get you motivated by " + selectedUsername)
          .addAudioPlayerPlayDirective('REPLACE_ALL', selectedVideoUrl, 'dsfdsffdfdsds', 0)
          .getResponse();
    } catch (err) {
      // handle the error safely
      // handle the error safely
      var backupAudios = [
        {"username": "garyvee", "videoLink":"https://scontent-iad3-1.cdninstagram.com/v/t50.16885-16/72486813_733809353751664_1964578616144253320_n.mp4?_nc_ht=scontent-iad3-1.cdninstagram.com&_nc_cat=104&oe=5D9C9EA5&oh=3942a371bad9e3dc3691e409fff7c70f"},
        {"username": "grantcardone", "videoLink": "https://scontent-iad3-1.cdninstagram.com/v/t50.2886-16/72096084_2432703670280960_5097864963764379356_n.mp4?_nc_ht=scontent-iad3-1.cdninstagram.com&_nc_cat=108&oe=5D9CD3E4&oh=d2f2e6984cb3b4faaa4e3736aa07cdc9"},
        {"username": "karlniilo", "videoLink": "https://scontent-iad3-1.cdninstagram.com/v/t50.2886-16/71835244_515486559026851_8853822854996684669_n.mp4?_nc_ht=scontent-iad3-1.cdninstagram.com&_nc_cat=107&oe=5D9C9577&oh=dbb67d452f49747785de9256c9874491"}
      ];
      var selectedProfile = backupAudios[Math.floor(Math.random()*backupAudios.length)];
      var selectedUsername = selectedProfile.username;
      var selectedVideoUrl = selectedProfile.videoLink;
      return handlerInput.responseBuilder
          .speak("Let's get you motivated by " + selectedUsername)
          .addAudioPlayerPlayDirective('REPLACE_ALL', selectedVideoUrl, 'dsfdsffdfdsds', 0)
          .getResponse();
      // return handlerInput.responseBuilder
      //   .speak("Looks like we are seeing more requests than usual.. please try again!")
      //   .getResponse();
    }
  },
};

const CancelAndStopIntentHandler = {
  canHandle(handlerInput) {
    return handlerInput.requestEnvelope.request.type === 'IntentRequest'
        && (handlerInput.requestEnvelope.request.intent.name === 'AMAZON.CancelIntent'
            || handlerInput.requestEnvelope.request.intent.name === 'AMAZON.StopIntent');
  },
  handle(handlerInput) {
    const speechText = 'Goodbye! Hope you are having a fantastic day!';

    return handlerInput.responseBuilder
        .speak(speechText)
        .addAudioPlayerStopDirective()
        .getResponse();
  },
};

const SessionEndedRequestHandler = {
  canHandle(handlerInput) {
    return handlerInput.requestEnvelope.request.type === 'SessionEndedRequest';
  },
  handle(handlerInput) {
    console.log(`Session ended with reason: ${handlerInput.requestEnvelope.request.reason}`);

    return handlerInput.responseBuilder.getResponse();
  },
};

const ErrorHandler = {
  canHandle() {
    return true;
  },
  handle(handlerInput, error) {
    console.log(`Error handled: ${error.message}`);

    return handlerInput.responseBuilder
        .speak('Sorry, I can\'t understand the command. Please say again.')
        .reprompt('Sorry, I can\'t understand the command. Please say again.')
        .getResponse();
  },
};

const skillBuilder = Alexa.SkillBuilders.custom();

exports.handler = skillBuilder
    .addRequestHandlers(
        PlayAudioIntentHandler,
        AboutIntentHandler,
        HelpIntentHandler,
        HitMeIntentHandler,
        CancelAndStopIntentHandler,
        SessionEndedRequestHandler
    )
    .addErrorHandlers(ErrorHandler)
    .lambda();

