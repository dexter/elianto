if (typeof String.prototype.startsWith != 'function') {
	// see below for better implementation!
	String.prototype.startsWith = function (str) {
		return this.slice(0, str.length) == str;
	};
}

if (typeof String.prototype.endsWith != 'function') {
	// see below for better implementation!
	String.prototype.endsWith = function (str) {
		return this.slice(-str.length) == str;
	};
}

function getUnderlyingSpot(e) {

	var clickedElement = undefined;

	// Read if it was pressed the right button of the mouse
	var isRightMB;
	if ("which" in e)  // Gecko (Firefox), WebKit (Safari/Chrome) & Opera
		isRightMB = e.which == 3;
	else if ("button" in e)  // IE, Opera
		isRightMB = e.button == 2;

	if (!isRightMB)
		return;

	if (e.target)
		clickedElement = e.target;
	else if (e.srcElement)
		clickedElement = e.srcElement;
	if (clickedElement.nodeType == 3) // defeat Safari bug
		clickedElement = clickedElement.parentNode

	if (!clickedElement.hasAttribute('spot'))
		return;

	return clickedElement.getAttribute('spot');
}

function getSelectionCharOffsetsWithin(showAlert) {

	if (!showAlert)
		showAlert = false;

	var start = 0,
		end = 0,
		overlap = false;
	var range, priorRange, element;
	if (typeof window.getSelection != "undefined") {
		if (window.getSelection().rangeCount > 0) {
			range = window.getSelection().getRangeAt(0);
			if (range.commonAncestorContainer.childNodes.length > 0)
				overlap = true;
			// We need to check that the selection starts and stops on the same field
			var startFieldElement = range.startContainer.parentNode;
			var invalid = false;
			while (!startFieldElement.hasAttribute('field')) {
				startFieldElement = startFieldElement.parentNode;
				if (!("hasAttribute" in startFieldElement)) {
					invalid = true;
					break;
				}
			}

			var endFieldElement = range.endContainer.parentNode;
			while (!endFieldElement.hasAttribute('field')) {
				endFieldElement = endFieldElement.parentNode;
				if (!("hasAttribute" in endFieldElement)) {
					invalid = true;
					break;
				}
			}

			if (!invalid && startFieldElement == endFieldElement) {
				element = startFieldElement;
				priorRange = range.cloneRange();
				priorRange.selectNodeContents(element);
				priorRange.setEnd(range.startContainer, range.startOffset);
				start = priorRange.toString().length;
				end = start + range.toString().length;
			} else if (!invalid && startFieldElement != endFieldElement) {
				console.log('Cross-field selection is not allowed!');
				if (showAlert)
					alert("The spot you selected is across two different fields (or paragraphs), please change the selection in order to create a spot.");
			}
		}
	} else {
		alert('Feature not currently supported by your browser!');
	}

//	} else if (typeof document.selection != "undefined" && (sel = document.selection).type != "Control") {
//		range = sel.createRange();
//		if (range.text.length > 0) {
//			element = range.parentElement();
//			if (range.childNodes.length === 0) {
//				priorRange = document.body.createTextRange();
//				priorRange.moveToElementText(element);
//				priorRange.setEndPoint("EndToStart", range);
//				start = priorRange.text.length;
//				end = start + range.text.length;
//			}
//		}
//	}

	return {
		start: start,
		end: end,
		field: element != undefined ? $(element).attr('field') : undefined,
		overlap: overlap
	};
}

function selectText(containerEl, selection) {
	if (window.getSelection) {
		var charIndex = 0, range = document.createRange();
		range.setStart(containerEl, 0);
		range.collapse(true);
		var nodeStack = [containerEl], node, foundStart = false, stop = false;

		while (!stop && (node = nodeStack.pop())) {
			if (node.nodeType == 3) {
				var nextCharIndex = charIndex + node.length;
				if (!foundStart && selection.start >= charIndex && selection.start <= nextCharIndex) {
					range.setStart(node, selection.start - charIndex);
					foundStart = true;
				}
				if (foundStart && selection.end >= charIndex && selection.end <= nextCharIndex) {
					range.setEnd(node, selection.end - charIndex);
					stop = true;
				}
				charIndex = nextCharIndex;
			} else {
				var i = node.childNodes.length;
				while (i--) {
					nodeStack.push(node.childNodes[i]);
				}
			}
		}

		var sel = window.getSelection();
		sel.removeAllRanges();
		sel.addRange(range);

	} else {
		alert('Feature not currently supported by your browser!');

//		var textRange = document.body.createTextRange();
//		textRange.moveToElementText(containerEl);
//		textRange.collapse(true);
//		textRange.moveEnd("character", savedSel.end);
//		textRange.moveStart("character", savedSel.start);
//		textRange.select();
	}
}

function clearTextSelection() {
	if (window.getSelection) {
		if (window.getSelection().empty) {  // Chrome
			window.getSelection().empty();
		} else if (window.getSelection().removeAllRanges) {  // Firefox
			window.getSelection().removeAllRanges();
		}
	} else if (document.selection) {  // IE?
		document.selection.empty();
	}
}