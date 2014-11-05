'use strict';

/* Controllers */
var dexterControllers = angular.module('dexterAnnotator.controllers', ['ngSanitize', 'ui.bootstrap', 'ngTable']);

dexterControllers.controller('TemplateCtrl', function ($scope, Auth, $location, $timeout) {
	// We need it to pass Auth service to the template, by using the scope object
	$scope.Auth = Auth;
	$scope.loginFailed = false;

	$scope.login = function(email, password) {
		Auth.login(email, password).then(function() {
			console.log('Login successful');
			// Manually close the dropdown menu
			$('#dropDownLoginMenu').dropdown('toggle');

		}).catch(function () {
			$scope.loginFailed = true;
			console.error('Login failed');
		});
	}

	$scope.logout = function() {
		Auth.logout();

		$timeout(function () {
			$location.path('/home');
		}, 100)
	}

	$scope.signup = function(firstName, lastName, email, password) {

		Auth.signup(firstName, lastName, email, password).then(function() {
			$scope.signUpError = '';
			console.log('Sign Up successful');
			// Manually close the modal window
			$('.modal.in').modal('hide');

		}).catch(function (error) {
			$scope.signUpError = 'Sign Up failed: ' + error;
			console.error('Sign Up failed: ' + error);
		});
	}
});

dexterControllers.controller('CtrlStartAnnotation', function ($scope, $location, Annotator) {
	Annotator.getNextDoc().$promise.then(function (nextDoc) {
		var url = commonMethods.getURL(nextDoc.user.uid, nextDoc.doc.docId, nextDoc.status);
		$location.path(url);
	}).catch(function () {
		$location.path('/home');
	});
});

dexterControllers.controller('CtrlStep1', function ($scope, $compile, $filter, $q, $timeout, $window, $location, $routeParams, Annotator, EntityLinker, Auth) {

	commonMethods.resetState($scope);
	Annotator.getDocument($routeParams.docId, $routeParams.userId).$promise
		.then(loadDocument)
		.then(checkPermission);

	Annotator.getAnnotationStatus($routeParams.docId, $routeParams.userId).$promise.then(function (data) {
		$scope.notes = data.commentsStep1;
	});

	$scope.disableNextLink = Annotator.getCollectionTasks().indexOf('step2') == -1;

	// Automatically change the document annotation and some counters when the underlying model changes
	$scope.$watch("{'spots':spots, 'document.raw_content': document.raw_content, 'used_fields': used_fields}", function () {
		commonMethods.annotateDocument($scope);
		updatePercentageWorkDone();
	}, true);

	$scope.$watch('showAllSpots', function () {
		commonMethods.showSpots($scope);
	});

	angular.element($window).bind('click', function (e) {
		// We need to call the method with some delay because sometimes
		// it happens that if a text is previously selected and we deselect it,
		// the getSelection() method still see it as selected.
		$timeout(function () {
			$scope.updateIsTextSelected();
		}, 10)
	});

	$('#document-template').contextmenu({
		target: '#context-menu',
		before: function (e, element) {
			$timeout(function () {
				e = e || window.event;
				// Some browser select the underlying word/field's content on pressing the right button of the mouse.
				// Other don't do it. We simulate this behaviour by selecting the text of the underlying spot, if an
				// user clicks on a spot. This way the contextual menu options are correctly showed to the user.
				$scope.selectTextUnderlyingSpot(e);
				$scope.updateIsTextSelected();
			});
			return true;
		}
	});

	function loadDocument(document) {
		commonMethods.loadDocument($scope, document);
		$scope.showAllSpots = !$scope.documentIsAnnotated;
		commonMethods.preloadImages($scope, $filter);
	}

	function checkPermission() {

		var unauthorizedMsg = "You are not authorized to request this page!";
		var user = Auth.getUser();

		// Check that the tasks to do on this collection include the step1
		if (Annotator.getCollectionTasks().indexOf("step1") == -1) {
			alert(unauthorizedMsg);
			$window.history.back();
			return;
		}

		$scope.disableSaving = user.isAdmin ? $routeParams.userId != user.uid : false;

		// Admin Users can do what they want...
		if (user.isAdmin)
			return;

		if (user.uid != $routeParams.userId) {
			alert(unauthorizedMsg);
			$location.path('/home');
			return;
		}

		Annotator.getNextDoc().$promise.then(function(nextDoc) {
			if (nextDoc.doc.docId != $routeParams.docId && !$scope.documentIsAnnotated) {
				console.error('checkPermission failed');
				alert($scope.unauthorizedMsg);
				$location.path('/home');
			}
		});
	}

	function updatePercentageWorkDone() {
		var numActiveSpots = 0;
		var numAnnotatedSpot = 0;

		angular.forEach($scope.spots, function (spot) {
			var spot_field = spot.field.startsWith('body_par') ? 'body' : spot.field;
			if ($scope.used_fields.indexOf(spot_field) == -1)
				return;

			if (spot.deleted)
				return;

			numActiveSpots++;
			if (spot.entities.selected)
				numAnnotatedSpot++;
		});

		$scope.percentageOfWorkDone = numActiveSpots === 0 ? 0 : Math.round(numAnnotatedSpot / numActiveSpots * 100);
	}

	$scope.changeSelectedSpot = function (spotId) {
		console.log({changeSelectedSpot: spotId, enabled: !$scope.selectedSpot});

		if ($scope.selectedSpot === spotId)
			$scope.selectedSpot = undefined;
		else
			$scope.selectedSpot = spotId;
		if ($scope.spots[spotId].deleted === false)
			clearTextSelection();

		// Reset the state of the add entity form
		$scope.addEntityUrl = '';
		$scope.addEntityForm.$setPristine();
	};

	$scope.changeSelectedEntity = function (spotId, entityId) {
		// Check the selected entity is inside the candidates entities
		var found = false;
		angular.forEach($scope.spots[spotId].entities.candidates, function (candidate) {
			if (!found && candidate.entity === entityId)
				found = true;
		});

		if (!found) {
			console.error('Selected entity ' + entityId + ' is not found in the list of candidate entities for the selected spot ' + spotId);
			return;
		}

		if ($scope.spots[spotId].entities.selected === entityId)
			$scope.spots[spotId].entities.selected = undefined;
		else
			$scope.spots[spotId].entities.selected = entityId;

		// The ghost spot here should be abilitated here and the cross-spots deleted
		if ($scope.spots[spotId].deleted) {
			$scope.spots[spotId].deleted = false;
			commonMethods.forceSpotsGoodness($scope, $scope.spots[spotId]);
		}

		console.log({changeSelectedEntity: $scope.spots[spotId].entities.selected !== undefined, spotId: spotId, entityId: entityId});
	};

	$scope.deleteAllUnannotatedSpot = function () {
		$scope.showAllSpots = false;
	};

	$scope.deleteSpot = function (spotId) {
		console.log({DeleteSpot: spotId});
		commonMethods.deleteSpot($scope, spotId);
	};

	$scope.addSpotFromSelectedText = function () {

		$scope.updateIsTextSelected();
		if ($scope.isTextSelected) {
			var selection = $scope.selectedText;
			var spot = $scope.createSpotFromSelection(selection);
			clearTextSelection();
			if ($scope.selectedSpot !== spot.spotId)
				$scope.changeSelectedSpot(spot.spotId);

			commonMethods.forceSpotsGoodness($scope, spot);
		}
	}

	$scope.createSpotFromSelection = function (selection) {

		if (!selection)
			return undefined;
		selection = $scope.restrictMentionArea(selection);
		var mention = $scope.document.raw_content[selection.field].substring(selection.start, selection.end);

		// Check if a spot (start,end,field) already exists but is currently deleted
		var spotAlreadyExists = undefined;
		angular.forEach($scope.spots, function (spot) {
			if (!spotAlreadyExists && selection.field === spot.field) {
				if (spot.start === selection.start && spot.end === selection.end) {
					spotAlreadyExists = spot;
				}
			}
		});

		var spot;
		if (spotAlreadyExists !== undefined) {
			spot = spotAlreadyExists;
			spot.deleted = false;
			console.log({ReactivatedSpot: spot});
		} else {
			var spotId = 0;
			do {
				spotId--;
			} while ($scope.spots[spotId] !== undefined);

			spot = {
				deleted: false,
				created: true,
				docId: $scope.document.docId,
				entities: {
					selected: undefined,
					candidates: []
				},
				field: selection.field,
				mention: mention,
				spotId: spotId,
				spotterId: 'user-defined',
				start: selection.start,
				end: selection.end
			};
			$scope.spots[spot.spotId] = spot;
			console.log({AddedSpot: spot});

			var resp = EntityLinker.getCandidatesForMention(mention);
			resp.$promise.then(function (candidates) {
				angular.forEach(candidates, function (candidate) {
					if ($scope.entities[candidate.id] === undefined) {
						$scope.entities[candidate.id] = candidate;
					}
					spot.entities.candidates.push({
						entity: candidate.id,
						score: 0
					})
				})
			});
		}

		return spot;
	};

	$scope.restrictMentionArea = function (selection) {
		if (selection.field) {
			var mention = $scope.document.raw_content[selection.field].substring(selection.start, selection.end);
			while (/\s/.test(mention.slice(0, 1))) {
				selection.start += 1;
				mention = mention.slice(1);
			}
			while (/\s/.test(mention.slice(-1))) {
				selection.end -= 1;
				mention = mention.slice(0, -1);
			}
		}
		return selection;
	}

	$scope.searchMention = function () {
		if ($scope.isTextSelected) {
			var selection = $scope.selectedText;
			selection = $scope.restrictMentionArea(selection);
			var mention = $scope.document.raw_content[selection.field].substring(selection.start, selection.end);

			var fieldElement = $('.panel-body .' + selection.field)[0];
			selectText(fieldElement, selection);

			var spot = $scope.createSpotFromSelection(selection);
			spot.deleted = true;
			if ($scope.selectedSpot != spot.spotId)
				$scope.changeSelectedSpot(spot.spotId);

		} else if ($scope.selectedSpot && $scope.spots[$scope.selectedSpot].deleted) {
			// Deactivate the "ghost" spot
			$scope.changeSelectedSpot($scope.selectedSpot);
		}
	}

	$scope.applyToSimilarMentions = function (spotId) {
		var spot = $scope.spots[spotId];
		if (!spot.entities.selected)
			return;
		var mention = $scope.document.raw_content[spot.field].substring(spot.start, spot.end).toLowerCase();
		if (!mention)
			return;

		var reg = /[^A-Za-z0-9]/;
		angular.forEach($scope.document.raw_content, function (fieldValue, fieldName) {
			var compactFieldName = fieldName;
			if (compactFieldName.startsWith('body_par_'))
				compactFieldName = 'body';
			if ($scope.used_fields.indexOf(compactFieldName) >= 0) {
				fieldValue = fieldValue.toLowerCase();
				var start = -mention.length;
				while ((start = fieldValue.indexOf(mention, start + mention.length)) >= 0) {
					// Check that the previous and the next character are both not alphanumeric
					if (reg.test(fieldValue.substring(start, start + 1)) ||
						reg.test(fieldValue.substring(start + mention.length, start + mention.length + 1))) {
						var createdSpot = $scope.createSpotFromSelection({field: fieldName, start: start, end: start + mention.length});

						if (createdSpot) {

							if (createdSpot.entities.candidates.length)
								createdSpot.entities.candidates = spot.entities.candidates;
							createdSpot.entities.selected = spot.entities.selected;

							// We need to check that there are no intersecting spots already annotated
							var intersectingSpots = commonMethods.retrieveIntersectingSpots($scope, createdSpot);
							angular.forEach(intersectingSpots, function (spotIdToCheck) {
								if (createdSpot && $scope.spots[spotIdToCheck].entities.selected) {
									commonMethods.deleteSpot($scope, createdSpot.spotId);
									createdSpot = undefined;
								}
							});
						}

						if (createdSpot)
							commonMethods.forceSpotsGoodness($scope, createdSpot);
					}
				}
			}
		});
	}

	$scope.selectTextUnderlyingSpot = function (e) {

		// We don't need to patch the selection if the user already selected some text
		var selection = getSelectionCharOffsetsWithin(true);
		selection = $scope.restrictMentionArea(selection);
		var isTextSelected = selection.start < selection.end && $scope.document.raw_content[selection.field] !== undefined;

		if (isTextSelected)
			return;

		var spotId = getUnderlyingSpot(e);
		if (!spotId)
			return;
		var spot = $scope.spots[spotId];

		var fieldElement = $('.panel-body .' + spot.field)[0];
		// The fields used in selectTecheckDocumentIsAnnotated
		// xt are both in spot and selection. We can use both of them, interchangeably
		selectText(fieldElement, spot);
	}

	$scope.updateIsTextSelected = function () {

		var selection = getSelectionCharOffsetsWithin();
		selection = $scope.restrictMentionArea(selection);
		$scope.isTextSelected = selection.start < selection.end && $scope.document.raw_content[selection.field] !== undefined;
		$scope.selectedText = $scope.isTextSelected ? selection : undefined;

		// Search for a spot with the same border of the selected text
		$scope.underlyingSpot = undefined;
		angular.forEach($scope.spots, function (spot) {
			if (!$scope.underlyingSpot && selection.field === spot.field) {
				if (spot.start === selection.start && spot.end === selection.end) {
					$scope.underlyingSpot = spot.spotId;
				}
			}
		});

		// Detect if the selected text changed. If this is true, we have to disable the "ghost" selectedSpot
		if ($scope.isTextSelected && $scope.selectedSpot && $scope.spots[$scope.selectedSpot].deleted) {
			var selectedSpot = $scope.spots[$scope.selectedSpot];
			var mentionSelectedText = $scope.document.raw_content[$scope.selectedText.field].substring($scope.selectedText.start, $scope.selectedText.end);
			var mentionSelectedSpot = $scope.document.raw_content[selectedSpot.field].substring(selectedSpot.start, selectedSpot.end);
			if (mentionSelectedSpot.toLowerCase() != mentionSelectedText.toLowerCase())
				$scope.changeSelectedSpot($scope.selectedSpot);
		}

		// Deactivate a selected spot if it was created by the search mention feature (the spot is a ghost...)
		if (!$scope.isTextSelected && $scope.selectedSpot && $scope.spots[$scope.selectedSpot].deleted) {
			$scope.changeSelectedSpot($scope.selectedSpot);
		}
	};

	$scope.addEntity = function (entityUrl) {
		var match = RegExp('^(?:http:\/\/)?(?:en.)?wikipedia.org/wiki/(.*)$').exec(entityUrl);
		if (!match) {
			$scope.addEntityForm.addEntityUrl.$setValidity('pattern', false);
			return;
		}
		var entityTitle = match[1];

		var resp = EntityLinker.getEntityFromTitle(entityTitle);
		resp.then(function (entity) {
			$scope.entities[entity.id] = entity;

			// Check that the element is not already in the array of entities. We need to avoid duplicate elements
			var found = false;
			angular.forEach($scope.spots[$scope.selectedSpot].entities.candidates, function (entityScope) {
				if (entityScope.entity === entity.id)
					found = true;
			});

			if (!found) {
				$scope.spots[$scope.selectedSpot].entities.candidates.push({
					entity: entity.id,
					score: 0
				});
			}

			if ($scope.spots[$scope.selectedSpot].entities.selected !== entity.id)
				$scope.changeSelectedEntity($scope.selectedSpot, entity.id);
			$scope.addEntityUrl = '';
			$scope.addEntityForm.$setPristine();

		}).catch(function (error) {

			// Sometimes the title need to be decoded because in the db he is saved this way
			var decodedEntityTitle = decodeURIComponent(entityTitle);
			if (decodedEntityTitle != entityTitle)
				return $scope.addEntity(decodedEntityTitle);

			console.error({addEntity: entityTitle, error: error});
			alert(error);
			$scope.addEntityForm.addEntityUrl.$setValidity('apiError', false);
		});
	};

	$scope.getSpotClass = function (spotId) {
		return commonMethods.getSpotClass($scope, spotId);
	};

	$scope.saveUserAnnotations = function (checkProgress) {

		if (checkProgress && $scope.percentageOfWorkDone < 100) {
			$('#checkModal').modal({
				backdrop: 'static',
				keyboard: false
			});

			return;

		} else if ($scope.percentageOfWorkDone < 100) {
			$scope.deleteAllUnannotatedSpot();
		}

		$scope.savingFailed = true;
		$scope.skipDocument = false;
		$scope.savingErrors = [];

		var promises = [];
		var savedSpot = [];
		angular.forEach($scope.spots, function (spot) {
			// Skip deleted and unannotated spots
			if (spot.deleted || !spot.entities.selected)
				return;

			savedSpot.push(spot.spotId);

			var deferred = $q.defer();
			if (spot.created && spot.spotId < 0) {
				// The first step is to create the spot on the server
				// params = { docid: docId, field: field, start: start, end: end, spot: mention }
				Annotator.createSpot({
					docid: $scope.document.docId,
					field: spot.field,
					start: spot.start,
					end: spot.end,
					spot: spot.mention
				}).$promise.then(function (newSpot) {
					var oldSpotId = spot.spotId;
					// copy some values from the created spot. The others are equals to the old spot
					angular.forEach(['spotId', 'spotterId', 'userId'], function (field) {
						spot[field] = newSpot[field];
					});

					$scope.spots[newSpot.spotId] = spot;
					delete($scope.spots[oldSpotId]);
					if ($scope.selectedSpot === oldSpotId)
						$scope.selectedSpot = newSpot.spotId;

					deferred.resolve(spot);
					console.log({CreatedSpot: spot});

				}).catch(function () {
					var msg = 'Problem with the APIs for creating a new spot'
					$scope.savingErrors.push(msg);
					deferred.reject(msg);
				});

			} else {
				deferred.resolve(spot);
			}

			promises.push(deferred.promise);

			var deferred2 = $q.defer();
			deferred.promise.then(function (spot) {
				// params = { docid: docId, spotid: spotId, e: entityId, spotter: spotter, score: score}

				Annotator.saveUserAnnotation({
					docid: $scope.document.docId,
					spotid: spot.spotId,
					e: spot.entities.selected,
					spotter: spot.spotterId
				}).$promise.then(function (userAnnotation) {
					if (userAnnotation.annotationId && userAnnotation.userScore !== undefined) {
						deferred2.resolve(userAnnotation);
						console.log({SavedUserAnnotation: userAnnotation});
					} else {
						console.log(userAnnotation);
						var msg = 'Problem with the APIs for saving user annotation of spot: ' + spot.spotId
						$scope.savingErrors.push(msg);
						deferred2.reject(msg);
					}
				}).catch(function () {
					var msg = 'Problem with the APIs for saving user annotation of spot: ' + spot.spotId
					$scope.savingErrors.push(msg);
					deferred2.reject(msg);
				});
			}).catch(function (e) {
				console.error(e);
			})

			promises.push(deferred2.promise);
		});

		// Compute the spots previously saved but now deleted (for a document previously annotated)
		var spotToDelete = $($scope.annotatedSpotAtBeginning).not(savedSpot).get();
		angular.forEach(spotToDelete, function (spotId) {

			var spot = $scope.spots[spotId];

			var defer = $q.defer();
			// params = { docid: docId, spotid: spotId}
			Annotator.deleteUserAnnotation({
				docid: $scope.document.docId,
				spotid: spot.spotId,
				spotter: spot.spotterId
			}).$promise.then(function (userAnnotation) {
				defer.resolve(userAnnotation);
				console.log({DeletedUserAnnotation: userAnnotation});
			}).catch(function () {
				var msg = 'Problem with the APIs for deleting user annotation of spot: ' + spot.spotId
				$scope.savingErrors.push(msg);
				defer.reject(msg);
			});

			promises.push(defer.promise);
		});

		if ($scope.notes) {
			var defer = $q.defer();
			Annotator.saveComment({
				docId: $scope.document.docId,
				status: 'STEP1',
				comment: $scope.notes
			}).$promise.then(function (annotationStatus) {
					defer.resolve(annotationStatus);
				}).catch(function () {
					var msg = 'Problem with the APIs for saving the notes';
					$scope.savingErrors.push(msg);
					defer.reject(msg);
				});
			promises.push(defer.promise);
		}

		$q.all(promises).then(function () {

			var nextStep = 'STEP2';
			if ($scope.disableNextLink)
				nextStep = 'DONE';

			Annotator.saveProgress($scope.document.docId, nextStep).$promise
				.then(function () {
					console.log('User Annotation saved correctly!!!');
					$scope.savingFailed = false;
				}).catch(function (error) {
					console.error('User Annotation saved correctly but progress NOT saved!!! ' + error);
					$scope.savingErrors.push('User Annotation saved correctly but progress NOT saved!!! ' + error);
				});

		}).catch(function (error) {
				console.error('User Annotation NOT saved correctly!!! ' + error);
			}).finally(function () {
				// show the modal window at the end of the saving process
				$('#savingModal').modal({
					backdrop: 'static',
					keyboard: false
				});
			});
	}

	$scope.moveToNextStep = function (delay) {
		var path = $location.path();
		path = path.replace('step1', 'step2');
		// We have to use a deferred change because on the other hand the modal remains opened...
		$timeout(function () {
			$location.url(path);
		}, delay);
	}

	$scope.skipDocument = function () {

		$scope.savingFailed = true;
		$scope.skippedDocument = true;
		$scope.savingErrors = [];

		var promises = [];

		// First of all, we should delete previously saved annotations
		if ($scope.documentIsAnnotated) {

			// Compute the spots previously saved but now to delete
			angular.forEach($scope.annotatedSpotAtBeginning, function (spotId) {

				var spot = $scope.spots[spotId];

				var defer = $q.defer();
				// params = { docid: docId, spotid: spotId}
				Annotator.deleteUserAnnotation({
					docid: $scope.document.docId,
					spotid: spot.spotId,
					spotter: spot.spotterId
				}).$promise.then(function (userAnnotation) {
					defer.resolve(userAnnotation);
					console.log({DeletedUserAnnotation: userAnnotation});
				}).catch(function () {
					var msg = 'Problem with the APIs for deleting user annotation of spot: ' + spot.spotId
					console.error(msg);
					$scope.savingErrors.push(msg);
					defer.reject(msg);
				});

				promises.push(defer.promise);
			});
		}

		$q.all(promises).then(function () {
			Annotator.saveProgress($scope.document.docId, 'SKIPPED').$promise
				.then(function () {
					console.log('Document saved as to skip!!!');
					$scope.savingFailed = false;
				}).catch(function (error) {
					var msg = 'Document NOT saved as to skip!!! ' + error;
					console.error(msg);
					$scope.savingErrors.push(msg);
				})
		}).finally(function () {
			// show the modal window at the end of the saving process
			$('#savingModal').modal({
				backdrop: 'static',
				keyboard: false
			});
		});
	}

	$scope.moveToNextDocument = function () {

		var path = undefined;
		Annotator.getNextDoc().$promise.then(function (nextDoc) {
			path = commonMethods.getURL(nextDoc.user.uid, nextDoc.doc.docId, nextDoc.status);

			// We have to use a deferred change because on the other hand the modal remains opened...
			$timeout(function () {
				$location.url(path);
			}, 500);

		}).catch(function (error) {
				console.error('Problem in retrieving the next document to annotate! ' + error);
			});
	}
});

dexterControllers.controller('CtrlStep2', function ($scope, $compile, $filter, $q, $timeout, $location, $routeParams, $window, Auth, Annotator) {

	$scope.relevanceDesc = {
		0: 'Not relevant',
		1: 'Partially relevant',
		2: 'Highly relevant',
		3: 'Top relevant'
	}

	$scope.disablePrevLink = Annotator.getCollectionTasks().indexOf('step1') == -1;

	commonMethods.resetState($scope);
	$scope.lastOrderingEntities = [];
	Annotator.getDocument($routeParams.docId, $routeParams.userId).$promise
		.then(loadDocument)
		.then(checkDocumentIsAnnotated)
		.then(checkPermission);

	Annotator.getAnnotationStatus($routeParams.docId, $routeParams.userId).$promise.then(function (data) {
		$scope.notes = data.commentsStep2;
	});

	// Automatically change the document annotation and some counters when the underlying model changes
	$scope.$watch("{'spots':spots, 'document.raw_content': document.raw_content, 'used_fields': used_fields}", function () {
		commonMethods.annotateDocument($scope);
	}, true);

	$scope.$watch("entities", function () {
		updateDistributionScore();
	}, true);

	$scope.$watch('showAllSpots', function () {
		commonMethods.showSpots($scope);
	});

	function loadDocument(document) {
		commonMethods.loadDocument($scope, document);
		$scope.showAllSpots = false;
		$scope.orderEntitiesByRelevance();
		commonMethods.preloadImages($scope, $filter);
	}

	function checkDocumentIsAnnotated() {

		var user = Auth.getUser();
		if (user.isAdmin && Annotator.getCollectionTasks().indexOf("step1") == -1)
			return true;

		if (!$scope.documentIsAnnotated) {
			console.error('checkDocumentIsAnnotated failed: the document is not annotated');
			alert("The document is not annotated. You are not authorized to request this page!");
			$location.path('/home');
		}
	}

	function checkPermission() {

		var unauthorizedMsg = "You are not authorized to request this page!";
		var user = Auth.getUser();

		$scope.disableSaving = user.isAdmin ? $routeParams.userId != user.uid : false;

		// Check that the tasks to do on this collection include the step2
		if (Annotator.getCollectionTasks().indexOf("step2") == -1) {
			alert(unauthorizedMsg);
			$window.history.back();
			return;
		}

		// Admin Users can do what they want...
		if (user.isAdmin)
			return;

		if (user.uid != $routeParams.userId) {
			alert(unauthorizedMsg);
			$location.path('/home');
			return;
		}

		Annotator.getNextDoc().$promise.then(function(nextDoc) {
			if (nextDoc.doc.docId != $routeParams.docId && !$scope.documentIsAnnotated) {
				console.error('checkPermission failed');
				alert(unauthorizedMsg);
				$location.path('/home');
			}
		});
	}

	$scope.changeSelectedSpot = function (spotId) {
		console.log({changeSelectedSpot: spotId, enabled: !$scope.selectedSpot});
		if ($scope.selectedSpot === spotId)
			$scope.selectedSpot = undefined;
		else
			$scope.selectedSpot = spotId;
	};

	$scope.getSpotClass = function (spotId) {
		return commonMethods.getSpotClass($scope, spotId);
	};

	function updateDistributionScore() {
		var scoreDistribution = {};
		for (var i = 0; i <= 3; i++)
			scoreDistribution[i] = 0;

		var dictEntities = {};
		angular.forEach($scope.spots, function (spot) {
			if (spot.entities.selected) {
				var selectedEntity = spot.entities.selected;
				if (!$scope.entities[selectedEntity]) {
					console.error('Entity with id: ' + selectedEntity + ' is not in the list of the entities');
					return;
				}

				if (dictEntities[selectedEntity] == undefined) {
					dictEntities[selectedEntity] = true;
					scoreDistribution[$scope.entities[selectedEntity].score]++;
				}
			}
		});

		$scope.scoreDistribution = scoreDistribution;
	}

	$scope.saveEntitiesRelevance = function () {

		$scope.savingFailed = true;
		$scope.savingErrors = [];

		var promises = [];
		angular.forEach($scope.spots, function (spot) {
			if (spot.entities.selected) {
				var deferred = $q.defer();
				// params = { docid: docId, spotid: spotId, e: entityId, spotter: spotter, score: score}
				Annotator.saveUserAnnotation({
					docid: $scope.document.docId,
					spotid: spot.spotId,
					e: spot.entities.selected,
					spotter: spot.spotterId,
					score: $scope.entities[spot.entities.selected].score
				}).$promise.then(function (userAnnotation) {
						deferred.resolve(userAnnotation);
						console.log({SavedEntityRelevance: userAnnotation});
					}).catch(function () {
						var msg = 'Problem with the APIs for saving relevance of entity: ' + spot.entities.selected;
						deferred.reject(msg);
						$scope.savingErrors.push(msg);
					});

				promises.push(deferred.promise);
			}
		});

		if ($scope.notes) {
			var defer = $q.defer();
			Annotator.saveComment({
				docId: $scope.document.docId,
				status: 'STEP2',
				comment: $scope.notes
			}).$promise.then(function (annotationStatus) {
					defer.resolve(annotationStatus);
				}).catch(function () {
					var msg = 'Problem with the APIs for saving the notes';
					$scope.savingErrors.push(msg);
					defer.reject(msg);
				});
			promises.push(defer.promise);
		}

		$q.all(promises).then(function () {

			Annotator.saveProgress($scope.document.docId, 'DONE').$promise
				.then(function () {
					console.log('Relevance of entities saved correctly!!!');
					$scope.savingFailed = false;
				}).catch(function (error) {
					var msg = 'Relevance of entities saved correctly but progress NOT saved!!! ' + error;
					console.error(msg);
					$scope.savingErrors.push(msg);
				});

		}).catch(function (error) {
				console.error('Relevance of entities NOT saved correctly!!! ' + error);
			}).finally(function () {
				$('#savingModal').modal({
					backdrop: 'static',
					keyboard: false
				});
			});
	}

	$scope.moveToNextDocument = function () {

		var path = undefined;
		Annotator.getNextDoc().$promise.then(function (nextDoc) {
			path = commonMethods.getURL(nextDoc.user.uid, nextDoc.doc.docId, nextDoc.status);

			// We have to use a deferred change because on the other hand the modal remains opened...
			$timeout(function () {
				$location.url(path);
			}, 500);

		}).catch(function (error) {
				console.error('Problem in retrieving the next document to annotate! ' + error);
			});
	}

	$scope.moveToPrevStep = function () {
		var path = $location.path();
		path = path.replace('step2', 'step1');
		// We have to use a deferred change because on the other hand the modal remains opened...
		$timeout(function () {
			$location.url(path);
		}, 0);
	}

	$scope.orderEntitiesByRelevance = function() {

		$scope.lastOrderingEntities = Object.keys($scope.entities).map(function(entityId) {
			return $scope.entities[entityId];
		});

		$scope.lastOrderingEntities.sort(function (entity1, entity2) {
			return entity1.score - entity2.score;
		});
	}

	$scope.lastOrderingFunction = function(entity) {

		return $scope.lastOrderingEntities.indexOf(entity);
	}
});

dexterControllers.controller('CtrlDashboard', function ($scope, $filter, $q, $location, ngTableParams, Auth, Annotator) {

	$scope.userAnnotations = [];
	$scope.collectionStatus = [];
	$scope.leaderBoard = [];
	$scope.users = {};

	$scope.annotationStats = {
		'numSpots': 0,
		'numDocs': 0
	};

	$scope.qUserAnnotation = $q.defer();
	var promises = [];
	promises.push(Annotator.getUserAnnotations().$promise);
	promises.push(Annotator.getCollectionStatus().$promise);
	promises.push(Annotator.getLeaderBoard().$promise);
	promises.push(Auth.getUsers().$promise);
	$q.all(promises).then(function (data) {
		$scope.userAnnotations = data[0];
		$scope.collectionStatus = data[1];
		$scope.leaderBoard = data[2];
		$scope.users = data[3];

		// Adding the user name field
		angular.forEach($scope.userAnnotations, function (annotation) {
			annotation.userName = '';
			annotation.uniqueUserName = '';
			if ($scope.users[annotation.user.uid]) {
				annotation.userName = $scope.users[annotation.user.uid].firstName + ' ' + $scope.users[annotation.user.uid].lastName;
				annotation.uniqueUserName = annotation.userName + ' (id:' + annotation.user.uid + ')';
			}
			// Compute the step to link from the dashboard link
			annotation.stepLink = 'step1';
			if (annotation.status.toLowerCase() == 'step2' || Annotator.getCollectionTasks().indexOf("step1") == -1)
				annotation.stepLink = 'step2';
		});
		var rank=0;
		angular.forEach($scope.leaderBoard, function (userStats) {
			userStats.userName = '';
			userStats.uniqueUserName = '';
			userStats.avgSpotsPerDocs = userStats.numSpots / userStats.numDocs;
			if ($scope.leaderBoard.length > 1)
				userStats.rank = ++rank;
			if ($scope.users[userStats.userId]) {
				userStats.userName = $scope.users[userStats.userId].firstName + ' ' + $scope.users[userStats.userId].lastName;
				userStats.uniqueUserName = userStats.userName + ' (id:' + userStats.userId + ')';
			}

			$scope.annotationStats['numSpots'] += userStats.numSpots;
			$scope.annotationStats['numDocs'] += userStats.numDocs;
		});

		// config annotated document table
		$scope.tableParamsAnnotations = new ngTableParams({
			page: 1,            // show first page
			count: 10,          // count per page
			sorting: {
				timestamp: 'desc'     // initial sorting
			},
			filter: {}					// initial filters are empty
		}, {
			total: $scope.userAnnotations.length, // length of data,
			filterDelay: 0,
			getData: function ($defer, params) {
				var filteredData = params.filter() ?
					$filter('filter')($scope.userAnnotations, params.filter()) :
					$scope.userAnnotations;
				var orderedData = params.sorting() ?
					$filter('orderBy')(filteredData, params.orderBy()) :
					filteredData;
				params.total(orderedData.length); // set total for recalculate pagination, cause filtering
				$defer.resolve(orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count()));
			}
		});

		$scope.tableParamsLeaderBoard = new ngTableParams({
			page: 1,            // show first page
			count: 10,          // count per page
			sorting: {
				numSpots: 'desc'     // initial sorting
			},
			filter: {}					// initial filters are empty
		}, {
			total: $scope.leaderBoard.length, // length of data,
			filterDelay: 0,
			getData: function ($defer, params) {
				var filteredData = params.filter() ?
					$filter('filter')($scope.leaderBoard, params.filter()) :
					$scope.leaderBoard;
				var orderedData = params.sorting() ?
					$filter('orderBy')(filteredData, params.orderBy()) :
					filteredData;
				params.total(orderedData.length); // set total for recalculate pagination, cause filtering
				$defer.resolve(orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count()));
			}
		});

		$scope.qUserAnnotation.resolve(data);

		// config collection status table
		$scope.tableParamsCollection = new ngTableParams({
			page: 1,            // show first page
			count: 10,          // count per page
			sorting: {
				freq: 'desc'    // initial sorting
			},
			filter: {}					// initial filters are empty
		}, {
			total: $scope.collectionStatus.length, // length of data,
			filterDelay: 0,
			getData: function ($defer, params) {
				var filteredData = params.filter() ?
					$filter('filter')($scope.collectionStatus, params.filter()) :
					$scope.collectionStatus;
				var orderedData = params.sorting() ?
					$filter('orderBy')(filteredData, params.orderBy()) :
					filteredData;
				params.total(orderedData.length); // set total for recalculate pagination, cause filtering
				$defer.resolve(orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count()));
			}
		});

	}).catch(function (error) {
			console.error(error);
		});

	$scope.uniques = function (column, fieldScope) {
		var def = $q.defer(),
			arr = [],
			names = [];
		$scope.qUserAnnotation.promise.then(function () {
			var columnName = Object.keys(column.filter)[0];
			angular.forEach($scope[fieldScope], function (item) {
				if (jQuery.inArray(item[columnName], arr) === -1) {
					arr.push(item[columnName]);
					names.push({
						'id': item[columnName],
						'title': item[columnName]
					});
				}
			});
			def.resolve(names);
		});
		return def;
	};
});

dexterControllers.controller('CtrlContact', function ($scope) {

});

dexterControllers.controller('CtrlHome', function ($scope) {

	$scope.over = function (btn) {
		$("#" + btn).stop().animate({
			width: 110
		}, 100);
	}
	$scope.out = function (btn) {
		$("#" + btn).stop().animate({
			width: 90
		}, 100);
	}

	$scope.next = function () {

		var viewed = $('.steps .selected');
		if (viewed.attr('id') != 'step_6') {
			viewed.animate({
				left: '150%'
			}, 400).removeClass('selected');
			viewed.next().addClass('selected').animate({
				left: '0%'
			}, 400);
		}
	}

	$scope.prev = function () {

		var viewed = $('.steps .selected');
		if (viewed.attr('id') != 'step_1') {
			viewed.animate({
				left: '150%'
			}, 400).removeClass('selected');
			viewed.prev().addClass('selected').animate({
				left: '0%'
			}, 400);
		}
	}

	$scope.showNext = function() {
		var viewed = $('.steps .selected');
		return viewed.next().length;
	}

	$scope.showPrev = function() {
		var viewed = $('.steps .selected');
		return viewed.prev().length;
	}

});

dexterControllers.controller('CtrlGuidelines', function ($scope) {

	$scope.over = function (btn) {
		$("#" + btn).stop().animate({
			width: 98
		}, 100);
	}
	$scope.out = function (btn) {
		$("#" + btn).stop().animate({
			width: 88
		}, 100);
	}

	$scope.next = function () {

		var viewed = $('.steps .selected');
		if (viewed.attr('id') != 'step_19') {
			viewed.animate({
				left: '150%'
			}, 400).removeClass('selected');
			viewed.next().addClass('selected').animate({
				left: '0%'
			}, 400);
		}
	}

	$scope.prev = function () {

		var viewed = $('.steps .selected');
		if (viewed.attr('id') != 'step_1') {
			viewed.animate({
				left: '150%'
			}, 400).removeClass('selected');
			viewed.prev().addClass('selected').animate({
				left: '0%'
			}, 400);
		}
	}

	$scope.showNext = function() {
		var viewed = $('.steps .selected');
		return viewed.next().length;
	}

	$scope.showPrev = function() {
		var viewed = $('.steps .selected');
		return viewed.prev().length;
	}
});

dexterControllers.controller('CtrlFaq', function ($scope) {
	$scope.faqs = [
		{
			title: 'There are yet a annotation datasets, why you need to do that?',
			content: 'Because all the existing datasets do not have a notion of aboutness for a entity given a document. We are interested in annotating the most important entities for a document.'
		},
		{
			title: 'I found two or more entities that could be ok for a spot',
			content: 'In order to keep things simple, we decided to annotate just one entity per spot, please choose the entity you think it\'s more specific, and leave a comment in the comment-box.'
		},
		{
			title: 'I do not know what entity to associate with the current spot',
			content: 'Try to investigate using google. If you feel that the entity is not relevant for the document do not annotate it. If you feel that the entity would be relevant, put a comment and skip the document.'
		},
		{
			title: 'I can not find the entity to associate with the current spot',
			content: 'Try to investigate using google/wikipedia. If you feel that the entity is not relevant for the document do not annotate it (but put a comment in the box ). If you feel that the entity would be really relevant, put a comment and skip the document.'
		},
		{
			title: 'I am not sure about the importance of an entity for a document',
			content: 'Ask yourself if the most important entities are enough to understand the content of the document. Have a look at the guidelines.'
		},
		{
			title: 'The document is noisy, I do not understand it',
			content: 'Skip it.'
		},
		{
			title: 'How do you create the dataset?',
			content: 'We used the CONLL dataset.'
		},
		{
			title: 'I notice you ask me to login using facebook or google, why?',
			content: 'It\'s just to make things simpler, we will store only your mail and your name. We will not share these data with third parties. The final dataset will be anonymized and will not contain your personal data.'
		}	
	]
});

var commonMethods = {

	resetState: function ($scope) {
		$scope.selectedSpot = undefined;
		$scope.isTextSelected = false;
		$scope.documentIsAnnotated = false;
		$scope.annotatedSpotAtBeginning = [];
		$scope.percentageOfWorkDone = 0;
		$scope.used_fields = [];
		$scope.notes = '';
	},

	loadDocument: function ($scope, document) {

		$scope.document = document.document;
		$scope.entities = document.description;
		angular.forEach($scope.entities, function (entity) {
			entity.score = 1;
		});

		var spots = {};
		var spotsToCheck = [];
		angular.forEach(document.spots, function (spot) {
			spot.deleted = false;
			spot.created = false;

			if (spot.annotation) {
				$scope.documentIsAnnotated = true;
				$scope.annotatedSpotAtBeginning.push(spot.spotId);
				if (spot.spotterId == 'user-defined') {
					// We need to check that the user created spot don't overlap with other spot. If it do, we need to set the colliding spots as deleted
					spotsToCheck.push(spot);
				}

				var found = false;
				angular.forEach(spot.entities.candidates, function (candidate) {
					if (candidate.entity === spot.annotation.entityId)
						found = true;
				});
				if (!found) {
					spot.entities.candidates.push({
						entity: spot.annotation.entityId,
						score: 0
					});
				}

				spot.entities.selected = spot.annotation.entityId;
				if (spot.annotation.userScore > 0)
					$scope.entities[spot.annotation.entityId].score = spot.annotation.userScore;

			} else
				spot.entities.selected = undefined;

			spots[spot.spotId] = spot;
		});
		$scope.spots = spots;

		angular.forEach(spotsToCheck, function (spot) {
			commonMethods.forceSpotsGoodness($scope, spot);
		});

		var raw_content = {};
		angular.forEach($scope.document.document.content, function (field) {
			if (field.value)
				raw_content[field.name] = field.value;
		});
		$scope.document.raw_content = raw_content;
		delete $scope.document.document;
		return document;
	},

	annotateDocument: function ($scope) {

		if (!$scope.document || !$scope.document.raw_content)
			return;

		var content = {};
		angular.forEach($scope.document.raw_content, function (fieldValue, fieldName) {
			content[fieldName] = commonMethods.annotateField($scope, fieldName, fieldValue);
		});

		var body = [];
		angular.forEach(content, function (fieldValue, fieldName) {
			if (fieldName.startsWith('body_par_'))
				body.push(fieldValue);
		});
		content['body'] = body.join('\n');

		$scope.document.annotated_content = content;
	},

	annotateField: function ($scope, fieldName, fieldValue) {
		var lastPos = 0;
		var newFieldValue = '';

		// We need to order the spots for start position
		var fieldSpots = [];
		angular.forEach($scope.spots, function (spot) {
			if (spot.field === fieldName && !spot.deleted)
				fieldSpots.push(spot);
		});

		fieldSpots.sort(function (spot1, spot2) {
			return spot1.start - spot2.start;
		});

		angular.forEach(fieldSpots, function (spot) {
			if (spot.start > lastPos)
				newFieldValue += fieldValue.substring(lastPos, spot.start);
			newFieldValue +=
				'<a href="javascript:void(0);" spot="' + spot.spotId + '"' +
					'ng-class="{selected: \'spotSelected\', done: \'spotDone\', todo: \'spotTodo\'}[getSpotClass(' + spot.spotId + ')]" ' +
					'ng-click="changeSelectedSpot(' + spot.spotId + ')">';
			newFieldValue += fieldValue.substring(spot.start, spot.end);
			newFieldValue += '</a>';
			lastPos = spot.end;
		});
		if (lastPos < fieldValue.length)
			newFieldValue += fieldValue.substring(lastPos, fieldValue.length);

		newFieldValue = '<span class="' + fieldName + ' body_par" field="' + fieldName + '">' + newFieldValue + '</span>';
		return newFieldValue;
	},

	getSpotClass: function ($scope, spotId) {
		// It happens that when we change the selection with a newly created spot it could not be there from the beginning
		if ($scope.spots[spotId] === undefined)
			return '';
		if ($scope.selectedSpot === spotId)
			return 'selected';
		else if ($scope.spots[spotId].entities.selected !== undefined)
			return 'done';
		else
			return 'todo';
	},

	getURL: function (userId, docId, status) {
		return '/' + status.toLowerCase() + '/userId/' + userId + '/docId/' + docId;
	},

	forceSpotsGoodness: function ($scope, spot) {
		var intersectingSpots = commonMethods.retrieveIntersectingSpots($scope, spot);
		angular.forEach(intersectingSpots, function (spotIdToDelete) {
			commonMethods.deleteSpot($scope, spotIdToDelete);
		});
	},

	retrieveIntersectingSpots: function($scope, spot) {
		var intersectingSpots = [];
		angular.forEach($scope.spots, function (spotToCheck) {
			if (spot.field === spotToCheck.field && spot.spotId !== spotToCheck.spotId) {
				if (Math.max(spot.start, spotToCheck.start) <= Math.min(spot.end, spotToCheck.end))
					intersectingSpots.push(spotToCheck.spotId);
			}
		});
		return intersectingSpots;
	},

	deleteSpot: function ($scope, spotId) {
		$scope.spots[spotId].deleted = true;
		if ($scope.selectedSpot == spotId)
			$scope.selectedSpot = undefined;
		// Reset the previously selected entity (if it was set)
		$scope.spots[spotId].entities.selected = undefined;
	},

	showSpots: function ($scope) {

		var annotatedSpots = [];
		angular.forEach($scope.spots, function (spot) {
			if ($scope.showAllSpots)
				spot.deleted = false;
			else if (!spot.entities.selected)
				commonMethods.deleteSpot($scope, spot.spotId);

			if (spot.entities.selected)
				annotatedSpots.push(spot);
		});

		// Sort the array so that the user-defined spots are pushed at the start
		annotatedSpots.sort(function (spot1, spot2) {
			var spot1Val = spot1.spotterId == 'user-defined' ? 0 : 1;
			var spot2Val = spot2.spotterId == 'user-defined' ? 0 : 1;
			return spot1Val - spot2Val;
		});

		angular.forEach(annotatedSpots, function (spot) {
			// If a previous spot has deleted the current one, we jump it (it are processed first the user-annotated spots)
			if (spot.deleted)
				return;
			commonMethods.forceSpotsGoodness($scope, spot);
		});

		if ($scope.selectedSpot && $scope.spots[$scope.selectedSpot].deleted)
			$scope.selectedSpot = undefined;
	},

	preloadImages: function ($scope, $filter) {

		angular.forEach($scope.entities, function (entity) {

			var imageResized = new Image();
			imageResized.onerror = function() {

				var imageFull = new Image();
				imageFull.onload = function() {
					// If the full size image is OK, but the reduced NOT (do nothing at now)
				};
				imageFull.onerror = function() {
					entity.image = 'images/no-image.png';
				};
				imageFull.src = entity.image;

			};
			imageResized.src = $filter('thumbnail')(entity.image);
		});
	}
};