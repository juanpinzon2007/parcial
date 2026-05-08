import { Injectable } from '@angular/core';
import { RuntimeConfig } from './models';
import { readRuntimeConfig } from './runtime-config';

@Injectable({
  providedIn: 'root'
})
export class ApiConfigService {
  private readonly config: RuntimeConfig = readRuntimeConfig();

  get authApiUrl(): string {
    return this.config.authApiUrl;
  }

  get inventoryApiUrl(): string {
    return this.config.inventoryApiUrl;
  }

  get salesApiUrl(): string {
    return this.config.salesApiUrl;
  }
}
