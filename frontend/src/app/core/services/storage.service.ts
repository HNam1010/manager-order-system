
import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { UserInfo } from '../../models/auth.model';

const TOKEN_KEY = 'auth-token';
const USER_KEY = 'auth-user';

@Injectable({
  providedIn: 'root'
})
export class StorageService {
  private isBrowser: boolean;

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  private getStorage(): Storage | null {
    return this.isBrowser ? window.localStorage : null;
  }

  clean(): void {
    const storage = this.getStorage();
    if (storage) {
      storage.clear();
      console.log('Storage cleared');
    }
  }

  public saveToken(token: string): void {
    const storage = this.getStorage();
    if (storage) {
      storage.removeItem(TOKEN_KEY);
      storage.setItem(TOKEN_KEY, token);
      console.log('Token saved:', token.substring(0, 20) + '...');
    } else {
      console.warn('StorageService: Cannot save token, not in browser environment.');
    }
  }

  public getToken(): string | null {
    const token = this.getStorage()?.getItem(TOKEN_KEY) ?? null;
    console.log('Retrieved token:', token ? token.substring(0, 20) + '...' : null);
    return token;
  }

  public removeToken(): void {
    const storage = this.getStorage();
    if (storage) {
      storage.removeItem(TOKEN_KEY);
      console.log('Token removed');
    }
  }

  public saveUser(user: UserInfo): void {
    const storage = this.getStorage();
    if (storage) {
      storage.removeItem(USER_KEY);
      const userString = JSON.stringify(user);
      storage.setItem(USER_KEY, userString);
      console.log('User saved:', user);
    } else {
      console.warn('StorageService: Cannot save user, not in browser environment.');
    }
  }

  public getUser(): UserInfo | null {
    const storage = this.getStorage();
    if (storage) {
      const user = storage?.getItem(USER_KEY);
      if (user) {
        try {
          const parsedUser = JSON.parse(user) as UserInfo;
          console.log('Retrieved user:', parsedUser);
          return parsedUser;
        } catch (e) {
          console.error('Error parsing user data from storage:', e);
          this.removeUser();
          return null;
        }
      }
      console.log('No user found in storage');
    }
    return null;
  }

  public removeUser(): void {
    const storage = this.getStorage();
    if (storage) {
      storage.removeItem(USER_KEY);
      console.log('User removed from storage');
    }
  }

  public isLoggedIn(): boolean {
    const isLoggedIn = !!this.getToken();
    console.log('isLoggedIn check:', isLoggedIn);
    return isLoggedIn;
  }
}