'use strict';

/* Filters */
var dexterFilters = angular.module('dexterAnnotator.filters', []);

dexterFilters.filter('interpolate', ['version', function (version) {
	return function (text) {
		return String(text).replace(/\%VERSION\%/mg, version);
	}
}]);

dexterFilters.filter('selectedSpotFilter', function() {
	return function(entities, spots, selectedSpot) {
		var candidateEntities = [];

		if (isNaN(selectedSpot) || spots == undefined || spots[selectedSpot] == undefined )
			return candidateEntities;

		angular.forEach(entities, function (entityValue, entityId) {
			angular.forEach(spots[selectedSpot].entities.candidates, function (candidateEntity, index) {
				if (candidateEntity.entity == entityId) {
					// We do that to maintain the order of the candidates entities
					candidateEntities[index] = entityValue;
				}
			});
		});

		return candidateEntities;
	}
});

dexterFilters.filter('annotatedEntityFilter', function() {
	return function(entities, spots) {
		var selectedEntities = [];
		var dictEntities = {};

		angular.forEach(spots, function (spot) {
			if (spot.entities.selected) {
				var selectedEntity = spot.entities.selected;
				if (!entities[selectedEntity]) {
					console.error('Entity with id: ' + selectedEntity + ' is not in the list of the entities');
					return;
				}

				if (dictEntities[selectedEntity] == undefined) {
					dictEntities[selectedEntity] = true;
					selectedEntities.push(entities[selectedEntity]);
				}
			}
		});

		return selectedEntities;
	}
});

dexterFilters.filter('selectedEntityFilter', function() {
	return function(entities, spots, selectedSpot) {
		if (!selectedSpot)
			return entities;

		var selectedEntities = [];
		var selectedEntity = spots[selectedSpot].entities.selected;
		if (!selectedEntity) {
			console.error('Spot with id: ' + selectedSpot + ' does not have an entity associated with');
			return selectedEntities;
		}
		angular.forEach(entities, function (entity) {
			if (entity.id === selectedEntity)
				selectedEntities.push(entity);
		});

		return selectedEntities;
	}
});

dexterFilters.filter('truncate', function () {
	return function (text, length, end) {
		if (isNaN(length))
			length = 128;

		if (end === undefined)
			end = "...";

		if (text.length <= length || text.length - end.length <= length) {
			return text;
		} else {
			return String(text).substring(0, length-end.length) + end;
		}
	};
});

dexterFilters.filter("leadingZeros", function() {
	return function(number, length) {
		if (isNaN(length))
			length = 3;
		if (!isNaN(number)) {
			var str = '' + number;
			while (str.length < length)
				str = '0' + str;
			return str;
		}
	};
});

dexterFilters.filter("thumbnail", function() {
	return function(url, size) {
		if (isNaN(size))
			size = 200;
		return url + '?size=' + size;
	};
});

dexterFilters.filter('encodeUrl', function() {
	return encodeURIComponent;
});