import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
// Chỉ import appConfig
import { appConfig } from './app/app.config';

// Sử dụng appConfig trực tiếp
bootstrapApplication(AppComponent, appConfig) // Sử dụng đối tượng appConfig đã import
  .catch((err) => console.error(err));
