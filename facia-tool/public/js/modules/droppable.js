/* global _: true */
define([
    'knockout',
    'modules/copied-article',
    'utils/draggable-element'
], function(
    ko,
    copiedArticle,
    draggableElement
) {
    var sourceGroup;
    var listeners = {
        dragstart: function (element, event) {
            var sourceItem = ko.dataFor(event.target);

            if (_.isFunction(sourceItem.get)) {
                event.dataTransfer.setData('sourceItem', JSON.stringify(sourceItem.get()));
            }
            sourceGroup = ko.dataFor(element);
        },
        dragover: function (element, event) {
            var targetGroup = ko.dataFor(element),
                targetItem = getTargetItem(event.target);

            event.preventDefault();
            event.stopPropagation();

            targetGroup.setAsTarget(targetItem);
        },
        dragleave: function (element, event) {
            var targetGroup = ko.dataFor(element);

            event.preventDefault();
            event.stopPropagation();

            targetGroup.unsetAsTarget();
        },
        drop: function (element, event) {
            var targetGroup = ko.dataFor(element),
                targetItem = getTargetItem(event.target),
                source;

            if (!targetGroup) {
                return;
            }

            try {
                source = draggableElement(event.dataTransfer, sourceGroup);
            } catch (ex) {
                window.alert(ex.message);
                return;
            }

            event.preventDefault();
            event.stopPropagation();

            copiedArticle.flush();

            targetGroup.unsetAsTarget();
            targetItem.drop(source, targetGroup);
        }
    };

    function getTargetItem (target, context) {
        context = context || ko.contextFor(target);
        var data = context.$data || {};
        if (!data.drop && context.$parentContext) {
            return getTargetItem(null, context.$parentContext);
        } else {
            return data;
        }
    }

    function getListener (name, element) {
        return function (event) {
            listeners[name](element, event);
        };
    }

    function init() {
        window.addEventListener('dragover', function(event) {
            event.preventDefault();
        },false);

        window.addEventListener('drop', function(event) {
            event.preventDefault();
        },false);

        ko.bindingHandlers.makeDropabble = {
            init: function(element) {
                for (var eventName in listeners) {
                    element.addEventListener(eventName, getListener(eventName, element), false);
                }
            }
        };
    }

    return {
        init: _.once(init),
        listeners: listeners
    };
});