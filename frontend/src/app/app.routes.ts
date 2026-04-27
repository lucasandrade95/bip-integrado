import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadChildren: () => import('./features/beneficio/beneficio.routes').then(m => m.BENEFICIO_ROUTES)
  }
];
