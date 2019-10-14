
function findProcessedCities(id, processedIds) {
    for(var i = 0; i < processedIds.length; i++) {
        if (id === processedIds[i]) {
            return true;
        }
    }
    return false;
}

function findCity(parseTree, cities, processedIds) {
    var lastLetterIndex = parseTree.text.length - 1; 
    var lastLetter = parseTree.text[lastLetterIndex]; 
    var count = Object.keys(cities).length;
    while (lastLetterIndex) { 
        for(var i = 1; i < count; i++) { 
            var city = cities[i];
            if (city.value.name[0].toLowerCase() === lastLetter.toLowerCase()) { 
                var isProcessed = findProcessedCities(city.id, processedIds); 
                if (!isProcessed) { 
                    return city;
                    }
                }
            }

        lastLetterIndex--; 
        lastLetter = parseTree.text[lastLetterIndex]; 
    }
    return null; 
  }

