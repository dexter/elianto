'use strict';

/* Services */
var dexterServices = angular.module('dexterAnnotator.services', ['ngResource', 'ngCookies']);

dexterServices.factory('Auth', function($resource, $http, $cookies, $q, appConfig, $timeout) {
	var service = {
		updated: false,
		user: undefined
	};

	service.getUser = function() {
		return service.user;
	}

	service.getUsers = function() {
		var user = service.user;
		var Users = $resource(
			appConfig.restApi + '/users',
			{ id:user.uid, psw:user.password },
			{ get: {method:'GET'} }
		);
		return Users.get();
	}

	service.login = function(email, psw) {

		// Initialize a new promise
		var deferred = $q.defer();

		var User = $resource(appConfig.restApi + '/login');
		User.get({email:email, psw:psw}, function(user) {

			// Positive answer
			service.user = user;
			service.updated = true;

			$cookies.uid = user.uid.toString();
			$cookies.psw = user.password;
			$cookies.mail = user.email;

			$timeout(function() {
				deferred.resolve(user);
			}, 0);

		}, function() {
			// Negative answer
			service.logout();

			$timeout(function() {
				service.updated = true;
				deferred.reject('Wrong Credentials');
			}, 0);

		});

		return deferred.promise;
	}

	service.updateLogIn = function() {
		var uid = $cookies.uid;
		var psw = $cookies.psw;

		// Initialize a new promise
		var deferred = $q.defer();

		if (uid && psw && !service.user) {

			var UserDetail = $resource(appConfig.restApi + '/user');
			UserDetail.get({id:uid, psw:psw}, function(user) {

				// Positive answer
				service.user = user;
				service.updated = true;

				$timeout(function() {
					deferred.resolve(user);
				}, 0);

			}, function() {
				// Negative answer
				service.logout();

				$timeout(function() {
					service.updated = true;
					deferred.resolve('Wrong Credentials');
				}, 0);

			});
		} else if (service.user) {
			$timeout(function() {
				service.updated = true;
				deferred.resolve(service.user);
			}, 0);
		} else {
			$timeout(function() {
				service.updated = true;
				deferred.resolve('Missing Credentials');
			}, 0);
		}

		return deferred.promise;
	}

	service.signup = function(firstName, lastName, email, psw) {

		// Initialize a new promise
		var deferred = $q.defer();

		var User = $resource(appConfig.restApi + '/signup');
		User.get({firstName: firstName, lastName: lastName, email:email, psw:psw}, function(user) {

			// Positive answer
			service.user = user;
			service.updated = true;

			$cookies.uid = user.uid.toString();
			$cookies.psw = user.password;
			$cookies.mail = user.email;

			$timeout(function() {
				deferred.resolve(user);
			}, 0);

		}, function(response) {

			$timeout(function() {
				service.updated = true;
				deferred.reject(response.data.error);
			}, 0);

		});

		return deferred.promise;
	}

	service.isUpdated = function() {
		return service.updated;
	}

	service.isLogged = function() {
		return Boolean(service.user);
	}

	service.logout = function() {
		$cookies.uid = undefined;
		$cookies.psw = undefined;
		$cookies.mail = undefined;
		service.user = undefined;
	}

	return service;
});

dexterServices.factory('Annotator', function($resource, $q, $timeout, Auth, appConfig) {
	var service = {
		tasks: undefined
	};

	service.getCollectionTasks = function() {
		return service.tasks;
	}

	service.updateCollectionTasks = function() {

		var CollectionTasks = $resource(appConfig.restApi + '/getCollectionTasks');

		// Initialize a new promise
		var deferred = $q.defer();

		if (!service.tasks) {

			CollectionTasks.get({coll: 'conll'}, function(tasks) {

				// Positive answer
				service.tasks = tasks.value.toLocaleLowerCase().split(',');

				$timeout(function() {
					deferred.resolve(service.tasks);
				}, 0);

			}, function() {

				console.error("Negative answer from updateCollectionTasks");

				// Negative answer
				service.tasks = undefined;
				$timeout(function() {
					deferred.reject('Problem retrieving the collection tasks!');
				}, 0);
			});
		} else {
			$timeout(function() {
				deferred.resolve(service.tasks);
			}, 0);
		}

		return deferred.promise;
	}

	service.getNextDoc = function() {

		if (!Auth.isLogged())
			return undefined;
		var user = Auth.getUser();

		var NextStep = $resource(
			appConfig.restApi + '/next',
			{ coll: 'conll', id:user.uid, psw:user.password },
			{ get: {method:'GET'} }
		);
		return NextStep.get();
	}

	service.getDocument = function(docId, userId) {

		if (!Auth.isLogged())
			return undefined;
		var user = Auth.getUser();
		if (!userId)
			userId = user.uid;

		var Document = $resource(
			appConfig.restApi + '/getAnnotatedDocument',
			{ coll: 'conll', id:user.uid, psw:user.password },
			{ get: {method:'GET'} }
		);
		return Document.get({ docid: docId, userid: userId });
	}

	service.saveProgress = function(docId, status) {

		if (!Auth.isLogged())
			return undefined;
		var user = Auth.getUser();

		var Progress = $resource(
			appConfig.restApi + '/saveProgress',
			{ id:user.uid, psw:user.password },
			{ save: {method:'GET'} }
		);
		return Progress.save({ docId: docId, status: status });
	}

	service.saveUserAnnotation = function(params) {
		// params = { docid: docId, spotid: spotId, e: entityId, spotter: spotter, score: score}
		if (!Auth.isLogged())
			return undefined;

		var user = Auth.getUser();
		var UserAnnotation = $resource(
			appConfig.restApi + '/setUserAnnotation',
			{ spotter: 'std', id: user.uid, psw: user.password },
			{ save: {method:'GET'} }
		);
		return UserAnnotation.save(params);
	}

	service.deleteUserAnnotation = function(params) {
		// params = { docid: docId, spotid: spotId}
		if (!Auth.isLogged())
			return undefined;

		var user = Auth.getUser();
		var UserAnnotation = $resource(
			appConfig.restApi + '/deleteUserAnnotation',
			{ id: user.uid, psw: user.password },
			{ save: {method:'GET'} }
		);

		return UserAnnotation.save(params);
	}

	service.getUserAnnotations = function(params) {
		// params = { coll: collection}
		if (!Auth.isLogged())
			return undefined;

		var user = Auth.getUser();
		var UserAnnotations = $resource(
			appConfig.restApi + '/userAnnotations',
			{ id: user.uid, psw: user.password, coll: 'conll' },
			{ get: {method:'GET', isArray: true} }
		);
		return UserAnnotations.get(params);
	}

	service.getCollectionStatus = function(params) {
		// params = { coll: collection}
		if (!Auth.isLogged())
			return undefined;

		var user = Auth.getUser();
		var CollectionStatus = $resource(
			appConfig.restApi + '/collectionStatus',
			{ id: user.uid, psw: user.password, coll: 'conll' },
			{ get: {method:'GET', isArray: true} }
		);
		return CollectionStatus.get(params);
	}

	service.getLeaderBoard = function(params) {
		// params = { coll: collection}
		if (!Auth.isLogged())
			return undefined;

		var user = Auth.getUser();
		var LeaderBoard = $resource(
			appConfig.restApi + '/leaderBoard',
			{ id: user.uid, psw: user.password, coll: 'conll' },
			{ get: {method:'GET', isArray: true} }
		);
		return LeaderBoard.get(params);
	}

	service.getAnnotationStatus = function(docId, userId) {
		if (!Auth.isLogged())
			return undefined;
		var user = Auth.getUser();
		if (!userId)
			userId = user.uid;

		var user = Auth.getUser();
		var UserAnnotations = $resource(
			appConfig.restApi + '/annotationStatus',
			{ id: user.uid, psw: user.password },
			{ get: {method:'GET'} }
		);
		return UserAnnotations.get({ docid: docId, userId: userId });
	}

	service.saveComment = function(params) {
		// params = { docId: docId, status: status, comment: comment}
		if (!Auth.isLogged())
			return undefined;

		var user = Auth.getUser();
		var UserAnnotations = $resource(
			appConfig.restApi + '/saveComment',
			{ id: user.uid, psw: user.password },
			{ get: {method:'GET'} }
		);
		return UserAnnotations.get(params);
	}

	service.createSpot = function(params) {
		// params = { docid: docId, field: field, start: start, end: end, spot: mention}
		if (!Auth.isLogged())
			return undefined;

		var user = Auth.getUser();
		var Spot = $resource(
			appConfig.restApi + '/createSpot',
			{ id:user.uid, psw:user.password },
			{ create: {method:'GET'} }
		);
		return Spot.create(params);
	}

	return service;
});

dexterServices.factory('EntityLinker', function($resource, $timeout, $q, appConfig) {
	var service = {};

	service.getCandidatesForMention = function(mention) {
		var EntityLinker = $resource(
			appConfig.restELApi + '/jsonp/get-candidates?query=:mention',
			{ callback:'JSON_CALLBACK', n: appConfig['numCandidatesPerSpot'], field: appConfig['fieldToUseForSpotting'] },
			{ getCandidates: {method:'JSONP', isArray: true} }
		);
		return EntityLinker.getCandidates({mention:mention});
	}

	service.getEntityFromTitle = function(title) {
		var EntityLinkerId = $resource(
			appConfig.restELApi + '/jsonp/get-id?title=:title',
			{ callback:'JSON_CALLBACK' },
			{ getId: {method:'JSONP'} }
		);

		var EntityLinkerDescription = $resource(
			appConfig.restELApi + '/jsonp/get-desc?id=:id',
			{ callback:'JSON_CALLBACK' },
			{ getDesc: {method:'JSONP'} }
		);

		// Initialize a new promise
		var deferred = $q.defer();

		EntityLinkerId.getId({title: title}, function(data) {

			if (data.id !== 0) {
				// Positive answer for getId
				EntityLinkerDescription.getDesc({id: data.id}, function(desc) {

					if (desc.title != '') {
						// Positive answer for getDesc
						$timeout(function() {
							deferred.resolve(desc);
						}, 0);
					} else {
						// Negative answer for getDesc
						$timeout(function() {
							deferred.reject('Description not found');
						}, 0);
					}

				}, function() {
					// Negative answer for getDesc
					$timeout(function() {
						deferred.reject('API server problem');
					}, 0);

				});
			} else {
				// Negative answer for getId
				$timeout(function() {
					deferred.reject('Title not found');
				}, 0);
			}

		}, function() {
			// Negative answer for getId
			$timeout(function() {
				deferred.reject('API server problem');
			}, 0);

		});

		return deferred.promise;
	}

	return service;
});
