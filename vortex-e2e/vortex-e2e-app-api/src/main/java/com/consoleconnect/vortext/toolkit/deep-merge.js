function deepMerge(obj1, obj2) {
    var result = JSON.parse(JSON.stringify(obj1));
    for (var key in obj2) {
      if (obj2.hasOwnProperty(key)) {
        if (typeof obj2[key] === 'object' && obj2[key] !== null && typeof result[key] === 'object' && result[key] !== null) {
          result[key] = deepMerge(result[key], obj2[key]);
        } else {
          result[key] = obj2[key];
        }
      }
    }
    return result;
}