


const DOMAIN_SUGGESTIONS = {
  'gmil.com': 'gmail.com',
  'gmal.com': 'gmail.com',
  'gmai.com': 'gmail.com',
  'gamil.com': 'gmail.com',
  'gnail.com': 'gmail.com',
  'gmail.co': 'gmail.com',
  'gmail.con': 'gmail.com',
  'gmail.cm': 'gmail.com',
  'gmail.om': 'gmail.com',
  'gmail.cpm': 'gmail.com',
  'gmail.comm': 'gmail.com',
  'gmaill.com': 'gmail.com',
  'gmial.com': 'gmail.com',
  'gmaul.com': 'gmail.com',
  'gmail.xom': 'gmail.com',
  'gmail.vom': 'gmail.com',
  'yaho.com': 'yahoo.com',
  'yahooo.com': 'yahoo.com',
  'yahoo.co': 'yahoo.com',
  'yahoo.con': 'yahoo.com',
  'yaho.in': 'yahoo.in',
  'yahoo.cm': 'yahoo.com',
  'hotmal.com': 'hotmail.com',
  'hotmai.com': 'hotmail.com',
  'hotmial.com': 'hotmail.com',
  'hotmail.co': 'hotmail.com',
  'hotmail.con': 'hotmail.com',
  'outlok.com': 'outlook.com',
  'outloo.com': 'outlook.com',
  'outlook.co': 'outlook.com',
  'outlook.con': 'outlook.com',
  'redifmail.com': 'rediffmail.com',
  'rediffmal.com': 'rediffmail.com',
  'rediffmail.co': 'rediffmail.com',
};


const VALID_DOMAINS = [
  'gmail.com', 'yahoo.com', 'yahoo.in', 'yahoo.co.in',
  'hotmail.com', 'outlook.com', 'live.com',
  'rediffmail.com', 'aol.com', 'icloud.com',
  'protonmail.com', 'zoho.com', 'mail.com',
  'yandex.com', 'gmx.com',
];


const DISPOSABLE_DOMAINS = [
  'mailinator.com', 'guerrillamail.com', 'tempmail.com', 'throwaway.email',
  'yopmail.com', 'sharklasers.com', 'guerrillamailblock.com', 'grr.la',
  'discard.email', 'trashmail.com', 'fakeinbox.com', 'temp-mail.org',
  'getnada.com', 'mailnesia.com', 'maildrop.cc', '10minutemail.com',
];


const COMMON_PASSWORDS = [
  'password', '123456', '12345678', '1234567890', 'qwerty', 'abc123',
  'password1', '111111', '123123', 'admin', 'letmein', 'welcome',
  'monkey', 'dragon', 'master', 'qwerty123', 'login', 'princess',
  'iloveyou', 'sunshine', 'password123', 'football', 'shadow',
];


const VALID_TLDS = [
  'com', 'in', 'co.in', 'net', 'org', 'edu', 'gov', 'io', 'co',
  'info', 'biz', 'me', 'us', 'uk', 'co.uk', 'ca', 'au', 'de',
  'fr', 'jp', 'ru', 'br', 'it', 'nl', 'se', 'no', 'fi', 'dk',
  'pl', 'cz', 'sk', 'hu', 'ro', 'bg', 'hr', 'si', 'rs', 'ba',
  'xyz', 'tech', 'online', 'store', 'app', 'dev', 'ai',
];



export function validateEmail(email) {
  if (!email || !email.trim()) {
    return { valid: false, error: 'Email is required' };
  }

  const trimmed = email.trim().toLowerCase();

  
  const basicRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  if (!basicRegex.test(trimmed)) {
    return { valid: false, error: 'Please enter a valid email address (e.g. name@gmail.com)' };
  }

  const [localPart, domain] = trimmed.split('@');

  
  if (localPart.length < 1) {
    return { valid: false, error: 'Email username is too short' };
  }
  if (localPart.length > 64) {
    return { valid: false, error: 'Email username is too long' };
  }
  if (localPart.startsWith('.') || localPart.endsWith('.')) {
    return { valid: false, error: 'Email cannot start or end with a dot' };
  }
  if (localPart.includes('..')) {
    return { valid: false, error: 'Email cannot have consecutive dots' };
  }

  
  if (!domain || domain.length < 3) {
    return { valid: false, error: 'Email domain is invalid' };
  }

  
  if (DISPOSABLE_DOMAINS.includes(domain)) {
    return { valid: false, error: 'Temporary/disposable email addresses are not allowed. Please use a real email.' };
  }

  
  if (DOMAIN_SUGGESTIONS[domain]) {
    const corrected = `${localPart}@${DOMAIN_SUGGESTIONS[domain]}`;
    return {
      valid: false,
      error: `Did you mean ${DOMAIN_SUGGESTIONS[domain]}?`,
      suggestion: corrected,
    };
  }

  
  const domainParts = domain.split('.');
  const tld = domainParts.slice(-2).join('.');
  const simpleTld = domainParts[domainParts.length - 1];

  if (!VALID_TLDS.includes(tld) && !VALID_TLDS.includes(simpleTld)) {
    
    if (simpleTld.length < 2) {
      return { valid: false, error: 'Email domain extension is invalid' };
    }
  }

  
  if (domainParts.length < 2) {
    return { valid: false, error: 'Email domain is incomplete (e.g. use gmail.com, not gmail)' };
  }

  return { valid: true };
}




export function validatePassword(password) {
  const checks = {
    minLength: password.length >= 6,
    maxLength: password.length <= 14,
    hasUppercase: /[A-Z]/.test(password),
    hasLowercase: /[a-z]/.test(password),
    hasNumber: /[0-9]/.test(password),
    hasSpecial: /[^A-Za-z0-9]/.test(password),
    notCommon: !COMMON_PASSWORDS.includes(password.toLowerCase()),
  };

  
  let strength = 0;
  if (checks.minLength) strength += 20;
  if (checks.hasUppercase) strength += 15;
  if (checks.hasLowercase) strength += 15;
  if (checks.hasNumber) strength += 20;
  if (checks.hasSpecial) strength += 20;
  if (checks.notCommon) strength += 10;

  if (!password) {
    return { valid: false, error: 'Password is required', checks, strength: 0 };
  }

  if (password.length > 14) {
    return { valid: false, error: 'Password cannot exceed 14 characters', checks, strength };
  }

  if (password.length < 6) {
    return { valid: false, error: 'Password must be at least 6 characters', checks, strength };
  }

  if (!checks.notCommon) {
    return { valid: false, error: 'This password is too common. Please choose a stronger one.', checks, strength: Math.min(strength, 20) };
  }

  
  const hasComplexity = checks.hasUppercase && checks.hasLowercase && checks.hasNumber;
  if (!hasComplexity) {
    return {
      valid: true, 
      error: null,
      warning: 'Add uppercase, lowercase & numbers for a stronger password',
      checks,
      strength,
    };
  }

  return { valid: true, checks, strength };
}




export function validatePhone(phone) {
  if (!phone || !phone.trim()) {
    return { valid: false, error: 'Mobile number is required', cleaned: '' };
  }

  
  let cleaned = phone.trim().replace(/[\s\-()]/g, '');

  
  if (cleaned.startsWith('+91')) cleaned = cleaned.slice(3);
  else if (cleaned.startsWith('91') && cleaned.length > 10) cleaned = cleaned.slice(2);
  else if (cleaned.startsWith('0') && cleaned.length > 10) cleaned = cleaned.slice(1);

  
  if (!/^\d+$/.test(cleaned)) {
    return { valid: false, error: 'Mobile number must contain only digits', cleaned };
  }

  if (cleaned.length !== 10) {
    return { valid: false, error: 'Mobile number must be exactly 10 digits', cleaned };
  }

  if (!/^[6-9]/.test(cleaned)) {
    return { valid: false, error: 'Mobile number must start with 6, 7, 8, or 9', cleaned };
  }

  
  if (/^(\d)\1{9}$/.test(cleaned)) {
    return { valid: false, error: 'Please enter a valid mobile number', cleaned };
  }

  return { valid: true, cleaned };
}




export function validateName(name) {
  if (!name || !name.trim()) {
    return { valid: false, error: 'Name is required' };
  }

  const trimmed = name.trim();

  if (trimmed.length < 2) {
    return { valid: false, error: 'Name must be at least 2 characters' };
  }

  if (trimmed.length > 50) {
    return { valid: false, error: 'Name cannot exceed 50 characters' };
  }

  if (!/^[a-zA-Z\u0900-\u097F\s.]+$/.test(trimmed)) {
    return { valid: false, error: 'Name can only contain letters, spaces, and dots' };
  }

  if (/\s{2,}/.test(trimmed)) {
    return { valid: false, error: 'Name cannot have consecutive spaces' };
  }

  return { valid: true };
}




export function validatePincode(pincode) {
  if (!pincode || !pincode.trim()) {
    return { valid: false, error: 'Pincode is required' };
  }

  const cleaned = pincode.trim();

  if (!/^\d{6}$/.test(cleaned)) {
    return { valid: false, error: 'Pincode must be exactly 6 digits' };
  }

  if (!/^[1-8]/.test(cleaned)) {
    return { valid: false, error: 'Invalid Indian pincode (must start with 1-8)' };
  }

  return { valid: true };
}




export function validateCity(city) {
  if (!city || !city.trim()) {
    return { valid: false, error: 'City is required' };
  }

  const trimmed = city.trim();

  if (trimmed.length < 2) {
    return { valid: false, error: 'City name must be at least 2 characters' };
  }

  if (trimmed.length > 50) {
    return { valid: false, error: 'City name is too long' };
  }

  if (!/^[a-zA-Z\u0900-\u097F\s]+$/.test(trimmed)) {
    return { valid: false, error: 'City name can only contain letters and spaces' };
  }

  return { valid: true };
}




export function validateAddress(address, required = true, minLength = 5) {
  if (!address || !address.trim()) {
    if (required) return { valid: false, error: 'Address is required' };
    return { valid: true };
  }

  const trimmed = address.trim();

  if (trimmed.length < minLength) {
    return { valid: false, error: `Address must be at least ${minLength} characters` };
  }

  if (trimmed.length > 200) {
    return { valid: false, error: 'Address is too long (max 200 characters)' };
  }

  return { valid: true };
}




export function validateAadhaar(aadhaar) {
  if (!aadhaar || !aadhaar.trim()) {
    return { valid: false, error: 'Aadhaar number is required' };
  }
  const cleaned = aadhaar.trim().replace(/\s/g, '');
  if (!/^\d{12}$/.test(cleaned)) {
    return { valid: false, error: 'Aadhaar must be exactly 12 digits' };
  }
  if (/^[01]/.test(cleaned)) {
    return { valid: false, error: 'Aadhaar number cannot start with 0 or 1' };
  }
  return { valid: true };
}




export function validatePAN(pan) {
  if (!pan || !pan.trim()) {
    return { valid: false, error: 'PAN number is required' };
  }
  const cleaned = pan.trim().toUpperCase();
  if (!/^[A-Z]{5}\d{4}[A-Z]$/.test(cleaned)) {
    return { valid: false, error: 'PAN must be in format: ABCDE1234F' };
  }
  return { valid: true };
}




export function validateOtp(otp) {
  if (!otp || !otp.trim()) {
    return { valid: false, error: 'OTP is required' };
  }
  if (!/^\d{6}$/.test(otp.trim())) {
    return { valid: false, error: 'OTP must be exactly 6 digits' };
  }
  return { valid: true };
}





export function digitsOnly(value) {
  return value.replace(/\D/g, '');
}


export function cleanPhone(value) {
  let cleaned = value.replace(/[\s\-()]/g, '').replace(/\D/g, '');
  if (cleaned.startsWith('91') && cleaned.length > 10) cleaned = cleaned.slice(2);
  if (cleaned.startsWith('0') && cleaned.length > 10) cleaned = cleaned.slice(1);
  return cleaned.slice(0, 10);
}


export function isPhoneInput(value) {
  const cleaned = value.trim().replace(/[\s\-+()]/g, '');
  return /^\d+$/.test(cleaned) && cleaned.length >= 7;
}
