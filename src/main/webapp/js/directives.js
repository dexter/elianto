'use strict';

/* Directives */

var dexterDirectives = angular.module('dexterAnnotator.directives', []);

dexterDirectives.directive('documentTemplate', function($compile, $window, $timeout) {
	return {
		restrict: 'A',
		transclude: 'true',
		link: function(scope, elem, attrs, controller, transclude) {

			// We need to observe changes on the template attribute because it will be filled by an async request
			attrs.$observe('template', function(value) {
				if (value) {
					// TODO: actually we are overwriting the template, still need to find a definitive solution
					scope.template =
						'<div class="title" annotate-field field="headline"></div>' +
						'<div class="dateline" annotate-field field="dateline"></div>' +
						'<div class="body" annotate-field field="body"></div>';

					var template = '<div ng-transclude></div>' + scope.template;

					elem.html(template);
					$compile(elem.contents(), transclude)(scope);
				}
			});
		}
	}
});

dexterDirectives.directive('annotateField', function($compile) {
	return {
		restrict: 'A',
		link: function(scope, elem, attrs) {
			scope.$watch('document.annotated_content.' + attrs.field, function(fieldValue) {
				// Add the field to the used_fields array if it's not already there
				// We use the array to keep track of the fields showed in the interface
				if (scope.used_fields.indexOf(attrs.field) == -1)
					scope.used_fields.push(attrs.field);
				if (elem.html() !== fieldValue) {
					elem.html(fieldValue);
					$compile(elem.contents())(scope);
				}
			});
		}
	}
});

dexterDirectives.directive('imgLoad', function() {
	return {
		restrict: 'A',
		scope: {
			ngSrc: '@'
		},
		link: function(scope, element, attrs) {
			element.on('load', function() {
				element.addClass('in');
			}).on('error', function() {
				var index = attrs.src.indexOf('?size=');
				if (index !== -1)
					attrs.$set('src', attrs.src.substr(0, index));
				else
					attrs.$set('src', 'images/no-image.png');
			});

			scope.$watch('src', function() {
				element.removeClass('in');
			});
		}
	};
});

dexterDirectives.directive('onRepeatFinished', function($timeout) {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			if (scope.$last === true) {
				$timeout(function () {
					scope.$emit('ngRepeatFinished');
				});
			}
		}
	};
});

dexterDirectives.directive('popoverConfig', function() {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			scope.$on('ngRepeatFinished', function (ngRepeatFinishedEvent) {
				$('.popover-markup > .trigger').popover({
					html : true,
					title: function() {
						return $(this).parent().find('.title').html();
					},
					content: function() {
						return $(this).parent().find('.content').html();
					},
					container: 'body',
					placement: 'left',
					trigger: 'hover'
				});
			});
		}
	};
});

dexterDirectives.directive("loadingIndicator", function() {
	return {
		restrict : "E",
		transclude: true,
		link : function(scope, element, attrs) {
			console.log('loadingIndicator');
			scope.$on("loading-started", function(e) {
				console.info('loading-started');
				element.addClass('in');
			});

			scope.$on("loading-complete", function(e) {
				console.info('loading-complete');
				element.removeClass('in');
			});
		}
	};
});

dexterDirectives.directive('stopEvent', function () {
	return {
		restrict: 'A',
		link: function (scope, element, attr) {
			element.bind(attr.stopEvent, function (e) {
				e.stopPropagation();
			});
		}
	};
});