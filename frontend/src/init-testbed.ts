import 'zone.js';
import 'zone.js/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed, getTestBed } from '@angular/core/testing';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting,
} from '@angular/platform-browser-dynamic/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { beforeEach } from 'vitest';
import { of } from 'rxjs';

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
  }
  throw error;
}

beforeEach(() => {
  TestBed.configureTestingModule({
    imports: [HttpClientTestingModule],
    providers: [
      {
        provide: ActivatedRoute,
        useValue: {
          paramMap: of(convertToParamMap({})),
          queryParamMap: of(convertToParamMap({})),
          params: of({}),
          queryParams: of({}),
          snapshot: {
            paramMap: convertToParamMap({}),
            queryParamMap: convertToParamMap({}),
            params: {},
            queryParams: {},
          },
        },
      },
      {
        provide: Router,
        useValue: {
          events: of(),
          url: '',
          navigate: () => Promise.resolve(true),
          createUrlTree: () => ({}),
          serializeUrl: () => '',
          isActive: () => false,
        },
      },
      { provide: MatDialogRef, useValue: { close: () => {} } },
      { provide: MAT_DIALOG_DATA, useValue: {} },
    ],
    schemas: [NO_ERRORS_SCHEMA],
  });
});
