'use strict';

// Declare app level module which depends on filters, and services
var dexterAnnotator = angular.module('dexterAnnotator', [
	'ngRoute',
	'dexterAnnotator.filters',
	'dexterAnnotator.controllers',
	'dexterAnnotator.services',
	'dexterAnnotator.directives'
])

dexterAnnotator.constant('appConfig', {
		'restApi': '/dexter-annotate/rest',
		'restELApi': 'http://node5.novello.isti.cnr.it:8080/dexter-webapp/api',
		'version': 0.5,
		'numCandidatesPerSpot': 10,
		'fieldToUseForSpotting': 'title'
	}
)

dexterAnnotator.config(function ($routeProvider) {
	$routeProvider.when('/home', {
		templateUrl: 'partials/home.html',
		controller: 'CtrlHome',
		requireLogin: false
	});
	$routeProvider.when('/guidelines', {
		templateUrl: 'partials/guidelines.html',
		controller: 'CtrlGuidelines',
		requireLogin: false
	});
	$routeProvider.when('/faq', {
		templateUrl: 'partials/faq.html',
		controller: 'CtrlFaq',
		requireLogin: false
	});
	$routeProvider.when('/contact', {
		templateUrl: 'partials/contact.html',
		controller: 'CtrlContact',
		requireLogin: false
	});
	$routeProvider.when('/annotate', {
		template: '',
		controller: 'CtrlStartAnnotation',
		requireLogin: true
	});
	$routeProvider.when('/step1/userId/:userId/docId/:docId', {
		templateUrl: 'partials/step1.html',
		controller: 'CtrlStep1',
		requireLogin: true
	});
	$routeProvider.when('/step2/userId/:userId/docId/:docId', {
		templateUrl: 'partials/step2.html',
		controller: 'CtrlStep2',
		requireLogin: true
	});
	$routeProvider.when('/dashboard', {
		templateUrl: 'partials/dashboard.html',
		controller: 'CtrlDashboard',
		requireLogin: true
	});
	$routeProvider.otherwise({redirectTo: '/home'});
});

dexterAnnotator.run(function($rootScope, $location, $route, Auth) {

	for (var path in $route.routes) {
		$route.routes[path].resolve = {
			loggedIn: function(Auth) {
				return Auth.updateLogIn();
			},
			collectionTasks: function(Annotator) {
				return Annotator.updateCollectionTasks();
			}
		};
	}

	$rootScope.$on('$routeChangeStart', function(e, curr, prev) {

		if (curr.$$route && curr.$$route.requireLogin && Auth.isUpdated() && !Auth.isLogged()) {
			alert("You need to be authenticated to see this page!");
			$location.path('/home');
		}
	});
});

dexterAnnotator.config(function($httpProvider) {
	$httpProvider.interceptors.push(function($q, $rootScope) {
		var counter = 0;

		var requestSent = function(config) {
			if (counter == 0)
				$rootScope.$broadcast('loading-started');
			counter++;
			return config || $q.when(config);
		};

		var responseReceived = function (response) {
			counter--;
			if (counter == 0)
				$rootScope.$broadcast('loading-complete');
			return response || $q.when(response);
		};

		var responseError = function (response) {
			counter--;
			if (counter == 0)
				$rootScope.$broadcast('loading-complete');
			return $q.reject(response);
		};

		return {
			'request': requestSent,
			'response': responseReceived,
			'responseError': responseError
		};
	});
});