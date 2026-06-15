











const SEARCH_ALIASES = {
  
  'ac': ['ac', 'air conditioner', 'air conditioning', 'ac servicing', 'ac repair', 'split ac', 'window ac', 'ac cleaning', 'ac gas'],
  'air conditioner': ['ac', 'air conditioner', 'ac servicing', 'ac repair'],
  'ac repair': ['ac', 'ac repair', 'ac servicing', 'air conditioner'],
  'ac service': ['ac', 'ac servicing', 'ac repair', 'ac cleaning', 'air conditioner'],
  'split ac': ['ac', 'split ac', 'ac servicing', 'air conditioner'],
  'window ac': ['ac', 'window ac', 'ac servicing', 'air conditioner'],
  
  'thanda': ['ac', 'air conditioner', 'ac servicing', 'cooling'],
  'thandi': ['ac', 'air conditioner', 'ac servicing', 'cooling'],
  'ac wala': ['ac', 'ac repair', 'ac servicing'],
  'ac thik': ['ac', 'ac repair', 'ac servicing'],
  'ac kharab': ['ac', 'ac repair'],
  'cooling': ['ac', 'air conditioner'],

  
  'washing machine': ['washing machine', 'washer', 'laundry'],
  'washer': ['washing machine', 'washer', 'laundry'],
  'wm': ['washing machine', 'washer'],
  'washing': ['washing machine', 'washer', 'laundry'],
  
  'dhulai': ['washing machine', 'laundry', 'cleaning'],
  'dhulai machine': ['washing machine', 'washer'],
  'kapde dhona': ['washing machine', 'laundry'],
  'kapde': ['washing machine', 'laundry'],

  
  'fridge': ['refrigerator', 'fridge', 'freezer'],
  'refrigerator': ['refrigerator', 'fridge', 'freezer'],
  'freezer': ['refrigerator', 'fridge', 'freezer'],
  
  'fridge wala': ['refrigerator', 'fridge repair'],
  'fridge kharab': ['refrigerator', 'fridge repair'],

  
  'electrician': ['electrician', 'electrical', 'wiring', 'switch', 'fan', 'light', 'mcb'],
  'electric': ['electrician', 'electrical', 'wiring'],
  'wiring': ['electrician', 'electrical', 'wiring'],
  'switch': ['electrician', 'switch', 'electrical'],
  'fan': ['electrician', 'fan', 'ceiling fan'],
  'light': ['electrician', 'light', 'tube light', 'bulb'],
  'bulb': ['electrician', 'light', 'bulb'],
  
  'bijli': ['electrician', 'electrical', 'wiring'],
  'bijli wala': ['electrician', 'electrical'],
  'bijlee': ['electrician', 'electrical', 'wiring'],
  'light wala': ['electrician', 'light'],
  'pankha': ['electrician', 'fan', 'ceiling fan'],
  'switch board': ['electrician', 'switch'],
  'mcb': ['electrician', 'mcb', 'electrical'],

  
  'plumber': ['plumber', 'plumbing', 'pipe', 'leak', 'tap', 'drain', 'water'],
  'plumbing': ['plumber', 'plumbing', 'pipe', 'leak'],
  'pipe': ['plumber', 'plumbing', 'pipe'],
  'leak': ['plumber', 'plumbing', 'leak', 'water leak'],
  'tap': ['plumber', 'tap', 'faucet'],
  'drain': ['plumber', 'drain', 'drainage', 'blocked drain'],
  'nalkaa': ['plumber', 'tap', 'plumbing', 'water'],
  
  'nal': ['plumber', 'tap', 'plumbing', 'water'],
  'pani': ['plumber', 'plumbing', 'water', 'ro', 'water purifier'],
  'pani wala': ['plumber', 'plumbing'],
  'nali': ['plumber', 'drain', 'drainage'],
  'paip': ['plumber', 'pipe', 'plumbing'],

  
  'carpenter': ['carpenter', 'carpentry', 'wood', 'furniture', 'door', 'cabinet'],
  'carpentry': ['carpenter', 'carpentry', 'wood', 'furniture'],
  'furniture': ['carpenter', 'furniture', 'assembly', 'table', 'bed', 'cabinet'],
  'door': ['carpenter', 'door', 'wood'],
  'cabinet': ['carpenter', 'cabinet', 'furniture', 'kitchen cabinet'],
  
  'mistri': ['carpenter', 'carpentry', 'handyman', 'home maintenance'],
  'lakdi': ['carpenter', 'carpentry', 'wood'],
  'lakdi wala': ['carpenter', 'carpentry'],
  'darwaza': ['carpenter', 'door'],
  'almaari': ['carpenter', 'cabinet', 'wardrobe', 'furniture'],
  'almari': ['carpenter', 'cabinet', 'wardrobe', 'furniture'],

  
  'painting': ['painting', 'paint', 'wall', 'color', 'whitewash', 'distemper'],
  'paint': ['painting', 'paint', 'wall'],
  'painter': ['painting', 'paint', 'painter'],
  'whitewash': ['painting', 'whitewash', 'wall'],
  'distemper': ['painting', 'distemper', 'wall'],
  
  'rang': ['painting', 'paint', 'color'],
  'rangai': ['painting', 'paint'],
  'deewar': ['painting', 'wall'],
  'safedi': ['painting', 'whitewash'],
  'putty': ['painting', 'putty', 'wall'],

  
  'cleaning': ['cleaning', 'deep cleaning', 'home cleaning', 'bathroom cleaning', 'kitchen cleaning'],
  'deep cleaning': ['cleaning', 'deep cleaning', 'home cleaning'],
  'bathroom cleaning': ['cleaning', 'bathroom', 'toilet', 'bathroom cleaning'],
  'kitchen cleaning': ['cleaning', 'kitchen', 'kitchen cleaning'],
  'sofa cleaning': ['sofa cleaning', 'carpet cleaning', 'upholstery', 'fabric cleaning'],
  'carpet cleaning': ['carpet cleaning', 'sofa cleaning', 'fabric cleaning'],
  
  'safai': ['cleaning', 'deep cleaning', 'home cleaning'],
  'saaf': ['cleaning', 'deep cleaning'],
  'ghar safai': ['cleaning', 'home cleaning', 'deep cleaning'],
  'bartan': ['cleaning', 'kitchen cleaning'],
  'bathroom safai': ['cleaning', 'bathroom cleaning'],
  'toilet': ['cleaning', 'bathroom cleaning', 'toilet'],
  'sofa safai': ['sofa cleaning', 'carpet cleaning'],

  
  'pest control': ['pest control', 'cockroach', 'termite', 'mosquito', 'bug'],
  'pest': ['pest control', 'bug', 'insect'],
  'cockroach': ['pest control', 'cockroach', 'bug'],
  'termite': ['pest control', 'termite'],
  'mosquito': ['pest control', 'mosquito'],
  
  'keede': ['pest control', 'bug', 'insect'],
  'keedey': ['pest control', 'bug', 'insect'],
  'makodi': ['pest control', 'cockroach'],
  'cockroach marna': ['pest control', 'cockroach'],
  'deemak': ['pest control', 'termite'],
  'macchar': ['pest control', 'mosquito'],

  
  'microwave': ['microwave', 'oven'],
  'oven': ['microwave', 'oven'],

  
  'ro': ['ro', 'water purifier', 'water filter'],
  'water purifier': ['ro', 'water purifier', 'water filter'],
  'water filter': ['ro', 'water purifier', 'water filter'],
  'ro service': ['ro', 'water purifier', 'ro service'],

  
  'geyser': ['geyser', 'water heater', 'heater'],
  'water heater': ['geyser', 'water heater'],
  'heater': ['geyser', 'water heater', 'heater'],

  
  'salon': ['salon', 'beauty', 'hair', 'facial', 'waxing', 'makeup'],
  'beauty': ['salon', 'beauty', 'facial', 'makeup'],
  'facial': ['salon', 'facial', 'beauty'],
  'waxing': ['salon', 'waxing', 'beauty'],
  'haircut': ['salon', 'haircut', 'hair'],
  'makeup': ['salon', 'makeup', 'beauty', 'bridal'],
  
  'baal': ['salon', 'haircut', 'hair'],
  'baal katna': ['salon', 'haircut'],
  'sundarta': ['salon', 'beauty'],

  
  'spa': ['spa', 'massage', 'relax', 'therapy', 'body massage'],
  'massage': ['massage', 'spa', 'relax', 'body massage', 'therapy'],
  'malish': ['massage', 'spa', 'body massage'],
  'maalish': ['massage', 'spa', 'body massage'],

  
  'garden': ['garden', 'gardening', 'lawn', 'plant', 'landscaping'],
  'gardening': ['garden', 'gardening', 'lawn', 'landscaping'],
  'lawn': ['garden', 'lawn', 'grass'],
  
  'bagicha': ['garden', 'gardening', 'plant'],
  'paudha': ['garden', 'plant', 'gardening'],

  
  'handyman': ['handyman', 'home maintenance', 'repair', 'general maintenance'],
  'repair': ['repair', 'home maintenance', 'handyman'],
  
  'theek karna': ['repair', 'home maintenance'],
  'tamir': ['repair', 'home maintenance', 'handyman'],

  
  'drill': ['drill', 'drills', 'drivers', 'power drill', 'electric drill'],
  'hammer': ['hammer', 'mallet', 'hammers'],
  'saw': ['saw', 'grinder', 'cutting'],
  'grinder': ['grinder', 'saw', 'angle grinder', 'cutting'],
  'wrench': ['wrench', 'pliers', 'spanner'],
  'pliers': ['pliers', 'wrench', 'cutter'],
  'ladder': ['ladder', 'scaffolding', 'step ladder'],
  'spanner': ['wrench', 'spanner', 'pliers'],
  
  'aari': ['saw', 'cutting'],
  'hathoda': ['hammer', 'mallet'],
  'seedhi': ['ladder', 'step ladder'],
  'pana': ['wrench', 'spanner'],
};







function wordBoundaryMatch(query, text) {
  if (!query || !text) return false;
  const q = query.toLowerCase().trim();
  const t = text.toLowerCase();

  
  
  if (q.length <= 3) {
    
    const words = t.split(/[\s\-_/,&()]+/);
    return words.some(word => word.startsWith(q));
  }

  
  try {
    const escaped = q.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const regex = new RegExp(`(?:^|[\\s\\-_/,&()])${escaped}`, 'i');
    return regex.test(t);
  } catch {
    return t.includes(q);
  }
}






function levenshteinDistance(a, b) {
  const matrix = [];
  for (let i = 0; i <= b.length; i++) matrix[i] = [i];
  for (let j = 0; j <= a.length; j++) matrix[0][j] = j;

  for (let i = 1; i <= b.length; i++) {
    for (let j = 1; j <= a.length; j++) {
      const cost = b[i - 1] === a[j - 1] ? 0 : 1;
      matrix[i][j] = Math.min(
        matrix[i - 1][j] + 1,       
        matrix[i][j - 1] + 1,       
        matrix[i - 1][j - 1] + cost  
      );
    }
  }
  return matrix[b.length][a.length];
}


function fuzzyWordMatch(query, word) {
  const q = query.toLowerCase();
  const w = word.toLowerCase();

  if (q.length < 4) return q === w; 
  if (q === w) return true;

  
  if (w.startsWith(q) || q.startsWith(w)) return true;

  const maxDist = q.length <= 6 ? 1 : 2;
  return levenshteinDistance(q, w) <= maxDist;
}







function expandQuery(rawQuery) {
  const q = rawQuery.toLowerCase().trim();
  const expanded = new Set();

  
  const queryWords = q.replace(/[^a-z0-9\s]/g, '').split(/\s+/).filter(w => w.length > 0);
  queryWords.forEach(w => expanded.add(w));

  
  if (SEARCH_ALIASES[q]) {
    SEARCH_ALIASES[q].forEach(term => expanded.add(term.toLowerCase()));
  }

  
  queryWords.forEach(word => {
    if (SEARCH_ALIASES[word]) {
      SEARCH_ALIASES[word].forEach(term => expanded.add(term.toLowerCase()));
    }
  });

  
  
  for (let len = 2; len <= queryWords.length; len++) {
    for (let start = 0; start <= queryWords.length - len; start++) {
      const phrase = queryWords.slice(start, start + len).join(' ');
      if (SEARCH_ALIASES[phrase]) {
        SEARCH_ALIASES[phrase].forEach(term => expanded.add(term.toLowerCase()));
      }
    }
  }

  
  queryWords.forEach(word => {
    if (word.length >= 4) {
      Object.keys(SEARCH_ALIASES).forEach(aliasKey => {
        
        if (!aliasKey.includes(' ') && fuzzyWordMatch(word, aliasKey)) {
          SEARCH_ALIASES[aliasKey].forEach(term => expanded.add(term.toLowerCase()));
        }
      });
    }
  });

  return expanded;
}







function expandSingleWord(word) {
  const w = word.toLowerCase().trim();
  const terms = new Set([w]);
  if (SEARCH_ALIASES[w]) {
    SEARCH_ALIASES[w].forEach(t => terms.add(t.toLowerCase()));
  }
  
  if (w.length >= 4) {
    Object.keys(SEARCH_ALIASES).forEach(key => {
      if (!key.includes(' ') && fuzzyWordMatch(w, key)) {
        SEARCH_ALIASES[key].forEach(t => terms.add(t.toLowerCase()));
      }
    });
  }
  return terms;
}


function scoreWordMatch(word, wordAliases, name, desc, category, providerName) {
  let bestScore = 0;

  for (const term of wordAliases) {
    
    if (name === term) bestScore = Math.max(bestScore, 100);
    else if (name.startsWith(term + ' ') || name.startsWith(term)) bestScore = Math.max(bestScore, 80);
    else if (wordBoundaryMatch(term, name)) bestScore = Math.max(bestScore, 60);

    
    if (wordBoundaryMatch(term, category)) bestScore = Math.max(bestScore, 40);

    
    if (wordBoundaryMatch(term, desc)) bestScore = Math.max(bestScore, 20);

    
    if (wordBoundaryMatch(term, providerName)) bestScore = Math.max(bestScore, 10);
  }

  return bestScore;
}


function scoreItem(item, _expandedTerms, rawQuery) {
  const name = (item.name || '').toLowerCase();
  const desc = (item.description || '').toLowerCase();
  const category = (item.category || '').toLowerCase();
  const providerName = (item.providerId?.name || item.providerName || '').toLowerCase();
  const q = rawQuery.toLowerCase().trim();

  
  const queryWords = q.replace(/[^a-z0-9\s]/g, '').split(/\s+/).filter(w => w.length > 0);
  
  if (queryWords.length === 0) return 0;

  
  const fullPhraseAliases = expandSingleWord(q);
  
  
  let fullPhraseScore = 0;
  for (const term of fullPhraseAliases) {
    if (name === term) fullPhraseScore = Math.max(fullPhraseScore, 100);
    else if (wordBoundaryMatch(term, name)) fullPhraseScore = Math.max(fullPhraseScore, 70);
    if (wordBoundaryMatch(term, desc)) fullPhraseScore = Math.max(fullPhraseScore, 25);
    if (wordBoundaryMatch(term, category)) fullPhraseScore = Math.max(fullPhraseScore, 40);
  }

  
  let totalWordScore = 0;
  let allWordsMatch = true;

  for (const word of queryWords) {
    const wordAliases = expandSingleWord(word);
    const wordScore = scoreWordMatch(word, wordAliases, name, desc, category, providerName);
    
    if (wordScore === 0) {
      allWordsMatch = false;
      break; 
    }
    totalWordScore += wordScore;
  }

  
  if (fullPhraseScore >= 60) {
    return fullPhraseScore;
  }

  if (!allWordsMatch) {
    return 0; 
  }

  return totalWordScore;
}








const GENERIC_CATEGORY_WORDS = new Set([
  'cleaning', 'clean', 'repair', 'service', 'services', 'maintenance',
  'professional', 'home', 'works', 'general', 'control', 'care',
  'appliance', 'electrical', 'personal', 'deep', 'full', 'complete',
  'scrub', 'pest', 'paint', 'wood', 'water',
  
  'safai', 'seva', 'kaam',
]);


function matchesCategoryKeywords(item, keywords, dbCategory) {
  if (!keywords || keywords.length === 0) return true;

  const name = (item.name || '').toLowerCase();
  const desc = (item.description || '').toLowerCase();

  
  const categoryWords = new Set();
  if (dbCategory) {
    dbCategory.toLowerCase().split(/[\s&,]+/).forEach(w => {
      if (w.length > 2) categoryWords.add(w);
    });
  }

  
  const specificKeywords = [];
  
  for (const kw of keywords) {
    const kwLower = kw.toLowerCase().trim();
    if (!kwLower) continue;
    
    
    const kwWords = kwLower.split(/\s+/);
    const isGeneric = kwWords.length === 1 && (
      GENERIC_CATEGORY_WORDS.has(kwLower) || 
      categoryWords.has(kwLower)
    );
    
    if (!isGeneric) {
      specificKeywords.push(kwLower);
    }
  }

  
  
  if (specificKeywords.length === 0) {
    return keywords.some(kw => {
      const keyword = kw.toLowerCase();
      return wordBoundaryMatch(keyword, name);
    });
  }

  
  
  
  return specificKeywords.some(kw => {
    return wordBoundaryMatch(kw, name);
  });
}







export function searchAndFilter(items, searchTerm, options = {}) {
  const {
    dbCategory = '',
    keywords = [],
    maxPrice = Infinity,
    minRating = 0,
    sortBy = 'relevance',
    priceField = 'basePrice',
    condition = 'All',
    onlyAvailable = false,
  } = options;

  const hasSearch = searchTerm && searchTerm.trim().length > 0;
  const expandedTerms = hasSearch ? expandQuery(searchTerm) : new Set();

  
  let results = items.map(item => {
    let passes = true;

    
    if (dbCategory && item.category !== dbCategory) {
      passes = false;
    }

    
    if (passes && dbCategory && keywords.length > 0) {
      if (!matchesCategoryKeywords(item, keywords, dbCategory)) {
        passes = false;
      }
    }

    
    let score = 0;
    if (passes && hasSearch) {
      score = scoreItem(item, expandedTerms, searchTerm);
      if (score === 0) passes = false;
    }

    
    const price = item[priceField] || 0;
    if (passes && price > maxPrice) passes = false;

    
    if (passes && (item.rating || 5) < minRating) passes = false;

    
    if (passes && condition !== 'All' && item.condition && item.condition !== condition) passes = false;

    
    if (passes && onlyAvailable && item.status !== 'available') passes = false;

    return { item, score, passes };
  }).filter(r => r.passes);

  
  if (sortBy === 'relevance' && hasSearch) {
    
    results.sort((a, b) => b.score - a.score);
  } else if (sortBy === 'lowestPrice' || sortBy === 'priceLowHigh') {
    results.sort((a, b) => (a.item[priceField] || 0) - (b.item[priceField] || 0));
  } else if (sortBy === 'highestPrice' || sortBy === 'priceHighLow') {
    results.sort((a, b) => (b.item[priceField] || 0) - (a.item[priceField] || 0));
  } else if (sortBy === 'highestRated') {
    results.sort((a, b) => (b.item.rating || 5) - (a.item.rating || 5));
  }

  return results.map(r => r.item);
}


export function getSearchSuggestions(query, maxResults = 8) {
  if (!query || query.trim().length < 2) return [];

  const q = query.toLowerCase().trim();
  const suggestions = [];

  
  Object.keys(SEARCH_ALIASES).forEach(key => {
    if (key.startsWith(q) || wordBoundaryMatch(q, key)) {
      suggestions.push(key);
    }
  });

  
  if (suggestions.length < 3 && q.length >= 4) {
    Object.keys(SEARCH_ALIASES).forEach(key => {
      if (!suggestions.includes(key) && fuzzyWordMatch(q, key)) {
        suggestions.push(key);
      }
    });
  }

  
  suggestions.sort((a, b) => {
    const aStarts = a.startsWith(q) ? 0 : 1;
    const bStarts = b.startsWith(q) ? 0 : 1;
    if (aStarts !== bStarts) return aStarts - bStarts;
    return a.localeCompare(b);
  });

  return suggestions.slice(0, maxResults);
}


export { wordBoundaryMatch, expandQuery, scoreItem, matchesCategoryKeywords, fuzzyWordMatch };
