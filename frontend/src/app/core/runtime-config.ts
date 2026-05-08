import { RuntimeConfig } from './models';

declare global {
  interface Window {
    __APP_CONFIG__?: Partial<RuntimeConfig>;
  }
}

const fallbackConfig: RuntimeConfig = {
  authApiUrl: 'http://localhost:8081/api',
  inventoryApiUrl: 'http://localhost:8082/api',
  salesApiUrl: 'http://localhost:8083/api'
};

function normalizeUrl(url: string | undefined, fallback: string): string {
  const value = (url ?? fallback).trim();
  return value.endsWith('/') ? value.slice(0, -1) : value;
}

export function readRuntimeConfig(): RuntimeConfig {
  const config = window.__APP_CONFIG__;

  return {
    authApiUrl: normalizeUrl(config?.authApiUrl, fallbackConfig.authApiUrl),
    inventoryApiUrl: normalizeUrl(config?.inventoryApiUrl, fallbackConfig.inventoryApiUrl),
    salesApiUrl: normalizeUrl(config?.salesApiUrl, fallbackConfig.salesApiUrl)
  };
}
