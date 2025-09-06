# JWT Token Refresh Implementation

## Overview
This document describes the comprehensive JWT token refresh implementation that automatically handles expired tokens and provides seamless user experience without requiring manual re-authentication.

## 🔧 **Backend Implementation**

### 1. **Enhanced JWT Utility (`JwtUtil.java`)**

#### New Features:
- **Dual Token System**: Access tokens (24 hours) and refresh tokens (7 days)
- **Token Type Validation**: Distinguishes between access and refresh tokens
- **Enhanced Error Handling**: Specific handling for expired tokens
- **Automatic Token Generation**: Methods for generating both token types

#### Key Methods:
```java
// Generate access token (24 hours)
public String generateToken(UserDetails userDetails)

// Generate refresh token (7 days)
public String generateRefreshToken(UserDetails userDetails)

// Validate refresh token specifically
public Boolean validateRefreshToken(String token, UserDetails userDetails)

// Check if token is a refresh token
public Boolean isRefreshToken(String token)

// Check if exception is due to expired token
public Boolean isTokenExpiredException(Exception e)
```

### 2. **Token Refresh Endpoint (`AuthController.java`)**

#### New Endpoint:
```
POST /api/auth/refresh
Authorization: Bearer <refresh_token>
```

#### Response Format:
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "new_access_token",
    "refreshToken": "new_refresh_token",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

### 3. **Automatic Token Refresh Interceptor (`JwtRefreshInterceptor.java`)**

#### Features:
- **Automatic Detection**: Detects expired access tokens
- **Seamless Refresh**: Automatically refreshes tokens when needed
- **Header Communication**: Sends new tokens via response headers
- **Error Handling**: Graceful fallback when refresh fails

#### Headers Added:
- `X-New-Access-Token`: New access token after refresh
- `X-Token-Refreshed`: Indicates token was refreshed

### 4. **Updated Security Configuration**

#### Changes:
- Added refresh interceptor before authentication filter
- Updated endpoint permissions for auth routes
- Enhanced CORS configuration

## 🎨 **Frontend Implementation**

### 1. **Token Manager (`tokenManager.js`)**

#### Features:
- **Automatic Token Storage**: Manages tokens in localStorage
- **Seamless Refresh**: Automatically refreshes expired tokens
- **Request Interception**: Handles API calls with token management
- **Auto-Refresh**: Proactively refreshes tokens before expiration
- **Error Handling**: Redirects to login on refresh failure

#### Key Methods:
```javascript
// Set tokens after login
setTokens(accessToken, refreshToken)

// Clear tokens on logout
clearTokens()

// Make authenticated requests with auto-refresh
async makeAuthenticatedRequest(url, options)

// Refresh token automatically
async refreshAccessToken()

// Check if user is authenticated
isAuthenticated()
```

### 2. **Enhanced Auth Service (`authService.js`)**

#### Features:
- **Axios Interceptors**: Automatic token attachment and refresh
- **Error Handling**: Automatic retry with new tokens
- **Token Storage**: Automatic token management
- **Logout Handling**: Clean token removal

#### API Client Features:
- **Request Interceptor**: Adds auth token to all requests
- **Response Interceptor**: Handles 401 errors and token refresh
- **Automatic Retry**: Retries failed requests with new tokens
- **Fallback**: Redirects to login when refresh fails

## 🔄 **Token Refresh Flow**

### 1. **Normal Request Flow**
```
1. User makes API request
2. Request interceptor adds access token
3. Server validates token
4. If valid, request proceeds normally
5. Response returned to user
```

### 2. **Expired Token Flow**
```
1. User makes API request
2. Request interceptor adds access token
3. Server detects expired token (401 error)
4. Response interceptor catches 401 error
5. Frontend calls refresh endpoint with refresh token
6. New access token received and stored
7. Original request retried with new token
8. Response returned to user
```

### 3. **Refresh Token Expired Flow**
```
1. User makes API request
2. Access token expired, refresh attempted
3. Refresh token also expired
4. User redirected to login page
5. Tokens cleared from storage
```

## ⚙️ **Configuration**

### Backend Configuration (`application.properties`)
```properties
# JWT configuration
jwt.secret.key=${JWT_SECRET}
jwt.expiration=86400000          # 24 hours
jwt.refresh.expiration=604800000 # 7 days
```

### Frontend Configuration
```javascript
// Token expiration times (in seconds)
const ACCESS_TOKEN_EXPIRY = 86400;  // 24 hours
const REFRESH_TOKEN_EXPIRY = 604800; // 7 days

// Auto-refresh threshold (5 minutes before expiry)
const AUTO_REFRESH_THRESHOLD = 300;
```

## 🚀 **Usage Examples**

### 1. **Login and Token Storage**
```javascript
import { login } from './service/authService';

const handleLogin = async (credentials) => {
  try {
    const response = await login(credentials);
    // Tokens automatically stored by authService
    console.log('Login successful');
  } catch (error) {
    console.error('Login failed:', error);
  }
};
```

### 2. **Making Authenticated Requests**
```javascript
import tokenManager from './util/tokenManager';

const fetchUserData = async () => {
  try {
    const response = await tokenManager.makeAuthenticatedRequest('/api/user/profile');
    return response.data;
  } catch (error) {
    console.error('Request failed:', error);
  }
};
```

### 3. **Manual Token Refresh**
```javascript
import { refreshToken } from './service/authService';

const handleRefresh = async () => {
  try {
    const tokens = await refreshToken();
    console.log('Tokens refreshed:', tokens);
  } catch (error) {
    console.error('Refresh failed:', error);
    // User will be redirected to login
  }
};
```

## 🔒 **Security Features**

### 1. **Token Security**
- **Short-lived Access Tokens**: 24-hour expiration
- **Long-lived Refresh Tokens**: 7-day expiration
- **Automatic Rotation**: New refresh token on each refresh
- **Secure Storage**: Tokens stored in localStorage

### 2. **Error Handling**
- **Graceful Degradation**: Fallback to login on refresh failure
- **No Token Exposure**: Tokens not logged or exposed in errors
- **Automatic Cleanup**: Tokens cleared on logout or error

### 3. **Rate Limiting**
- **Refresh Endpoint**: Protected by rate limiting
- **Request Throttling**: Prevents abuse of refresh mechanism
- **IP-based Limiting**: 100 requests per minute per IP

## 📊 **Performance Benefits**

### Before Implementation:
- ❌ Users logged out on token expiration
- ❌ Manual re-authentication required
- ❌ Poor user experience
- ❌ Lost session data

### After Implementation:
- ✅ Seamless token refresh
- ✅ No user interruption
- ✅ Automatic retry of failed requests
- ✅ Proactive token refresh
- ✅ Better user experience

## 🛠️ **Troubleshooting**

### Common Issues:

#### 1. **Token Refresh Fails**
```javascript
// Check if refresh token exists
if (!tokenManager.getRefreshToken()) {
  // Redirect to login
  window.location.href = '/login';
}
```

#### 2. **Infinite Refresh Loop**
```javascript
// Ensure retry flag is set
if (error.response?.status === 401 && !originalRequest._retry) {
  originalRequest._retry = true;
  // ... refresh logic
}
```

#### 3. **Token Not Attached**
```javascript
// Check token manager initialization
if (typeof window !== 'undefined') {
  tokenManager.initialize();
}
```

## 🔍 **Monitoring and Debugging**

### Backend Logs:
```
INFO  - Token refresh successful for user: user@example.com
WARN  - Access token expired for request: /api/user/profile
ERROR - Refresh token expired for user: user@example.com
```

### Frontend Console:
```
Token auto-refreshed
Token refresh failed: Invalid refresh token
Redirecting to login due to token refresh failure
```

## 📈 **Metrics to Monitor**

1. **Token Refresh Rate**: How often tokens are refreshed
2. **Refresh Success Rate**: Percentage of successful refreshes
3. **Login Rate**: How often users need to re-login
4. **API Error Rate**: 401 errors due to token issues
5. **User Session Duration**: How long users stay logged in

## 🎯 **Best Practices**

1. **Always use the token manager** for API calls
2. **Handle refresh errors gracefully** in UI
3. **Monitor token refresh metrics** in production
4. **Set appropriate token expiration times** for your use case
5. **Implement proper logout** to clear all tokens
6. **Test token refresh flow** thoroughly
7. **Monitor for infinite refresh loops**

## 🚀 **Deployment Considerations**

1. **Environment Variables**: Ensure JWT secrets are properly set
2. **HTTPS**: Use HTTPS in production for token security
3. **Token Storage**: Consider using httpOnly cookies for better security
4. **Monitoring**: Set up alerts for high refresh failure rates
5. **Testing**: Test token refresh flow in staging environment

---

This implementation provides a robust, user-friendly token management system that eliminates the need for manual re-authentication while maintaining security best practices.


