import 'zone.js';
import 'zone.js/testing';
import { getTestBed } from '@angular/core/testing';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting,
} from '@angular/platform-browser-dynamic/testing';

const testBed = getTestBed();
try {
  testBed.initTestEnvironment(
    BrowserDynamicTestingModule,
    platformBrowserDynamicTesting(),
  );
} catch (error) {
  if (
    error instanceof Error &&
    error.message.includes('Cannot reinitialize the TestBed')
  ) {
    // TestBed already initialized in this worker.
    // No-op.
  } else {
    throw error;
  }
}
