# StockPro Frontend — React + TypeScript Port

This is a **1:1 tech-stack port** of the Angular application at
`../StockPro-Frontend-dev` to React + TypeScript. No feature, route, permission
rule, validation rule, API contract, or visual style was intentionally changed —
only the framework/tooling underneath it.

Run it with:

```bash
npm install
npm run dev      # http://localhost:4200  (same port as `ng serve`)
npm run build    # production build to dist/
```

Backend API URL is read from `VITE_API_URL` (see `.env` / `.env.development`),
mirroring the Angular `environment.ts` / `environment.development.ts` files
(`http://localhost:8080` by default).

---

## Tech stack mapping

| Angular (before)                                    | React (after)                                   |
|------------------------------------------------------|--------------------------------------------------|
| Angular CLI + `@angular/build`                       | Vite                                              |
| Angular components (standalone, `.ts`/`.html`/`.scss`) | React function components (`.tsx` + `.css`)     |
| Angular Router (`provideRouter`, `loadComponent`)     | React Router v6 (`react-router-dom`)              |
| `CanActivateFn` guards (`auth-guard`, `role-guard`)   | Route-wrapper components using `<Outlet />` / `<Navigate />` (`AuthGuard`, `RoleGuard`) |
| `HttpClient` + `HttpInterceptorFn` (`jwt-interceptor`) | `axios` instance with request/response interceptors (`src/core/http.ts`) |
| `@Injectable` services (RxJS `Observable`)            | Plain TS objects with `async` methods (`Promise`) returning the same shape |
| RxJS `BehaviorSubject` (`alert.ts` `unreadCount$`)    | Minimal pub-sub (`subscribeUnreadCount`) inside `alertService`, consumed via `useEffect` |
| Angular Material (`mat-table`, `mat-dialog`, `mat-sidenav`, `mat-select`, `mat-snack-bar`, `mat-form-field`, ...) | MUI / Material UI v6 (`@mui/material`, `@mui/icons-material`) — the official React Material Design library, chosen specifically to keep the same look and interaction model |
| Template-driven forms (`ngModel`, `NgForm`, Reactive Forms `FormGroup`/`FormArray`) | Local component state (`useState`) with the same required/min/max/pattern checks, same touched-based error messages |
| `styles.scss` (global styles, `status-chip`, banners) | `src/index.css` (same selectors/colors, ported 1:1) |
| Per-component `.scss` files                           | Per-component `.css` files, same class names/values |
| `environment.ts` / `environment.development.ts`       | Same two files, values sourced from Vite's `import.meta.env` / `.env*` |
| `zone.js` + Angular change detection                  | Not needed — React's own render model |

Nothing about the **backend contract** changed: every service still calls the
exact same REST endpoints (`/api/v1/...`) with the same HTTP verbs, the same
request/response shapes, and the same `Authorization: Bearer <token>` header
injection + 401 → redirect-to-login behavior as the Angular `jwt-interceptor`.

---

## Structure (mirrors the Angular `src/app` layout)

```
src/
  core/
    guards/          auth-guard.tsx, role-guard.tsx      (was: auth-guard.ts, role-guard.ts)
    http.ts                                              (was: interceptors/jwt-interceptor.ts + app.config.ts wiring)
    models/           alert.ts, movement.ts, product.ts, purchase-order.ts,
                      report.ts, supplier.ts, user.ts, warehouse.ts     (unchanged field-for-field)
    services/         alert.ts, auth.ts, movement.ts, product.ts,
                      purchase-order.ts, report.ts, supplier.ts, warehouse.ts
  environments/       environment.ts, environment.development.ts
  shared/
    components/
      layout/         Layout.tsx, Layout.css                (was: layout.ts/.html/.scss)
      SnackbarHost.tsx                                       (thin wrapper around MUI Snackbar, replicates `MatSnackBar.open(message, 'Close', {duration})`)
    hooks/useSnackbar.ts
    utils/format.ts                                          (was: Angular `currency` / `date` pipes — `formatCurrencyINR`, `formatMediumDate`, `formatMediumDateTime`)
  features/
    auth/login/, auth/register/
    dashboard/
    products/product-list/       ProductList.tsx + ProductDialog.tsx
    suppliers/supplier-list/     SupplierList.tsx + SupplierDialog.tsx
    warehouses/warehouse-list/   WarehouseList.tsx + WarehouseDialog.tsx +
                                  WarehouseStockDialog.tsx + WarehouseTransferDialog.tsx
    purchase-orders/po-list/     PoList.tsx + PoDialog.tsx (dynamic item rows, same add/remove/min-1-row rule)
    movements/movement-list/
    alerts/alert-list/
    reports/report-dashboard/
    users/user-list/
  App.tsx             route table (was: app.routes.ts)
  main.tsx            bootstrap (was: main.ts + app.config.ts)
  theme.ts            MUI theme tuned to Angular Material's "indigo-pink" prebuilt theme
```

Every Angular Material dialog component (`ProductDialogComponent`,
`SupplierDialogComponent`, `WarehouseDialogComponent`,
`WarehouseStockDialogComponent`, `WarehouseTransferDialogComponent`,
`PoDialogComponent`) has a matching MUI `Dialog`-based React component with the
same fields, same validation rules (required / min / min-length / max-length /
email / 10-digit phone pattern), and the same disabled-until-valid Save button.

---

## Behavior preserved exactly

- **Routing**: `/` → redirect to `/dashboard`; `/login`, `/register` public;
  everything else behind the auth guard and rendered inside the sidenav
  `Layout`; `/alerts` and `/reports` require `ADMIN`/`MANAGER`; `/users`
  requires `ADMIN`; unknown paths redirect to `/dashboard`.
- **Auth/role logic**: `authService` exposes the same predicates
  (`isLoggedIn`, `isAdmin`, `isAdminOrManager`, `hasAnyRole`,
  `canAccessAlerts`, `canAccessReports`, `canManageUsers`,
  `canWriteInventory`) with identical rules, backed by the same
  `localStorage` keys (`token`, `role`, `fullName`, `email`).
- **JWT handling**: every request gets `Authorization: Bearer <token>` when a
  token is present; a `401` response clears storage and redirects to
  `/login`, same as `jwt-interceptor.ts`.
- **Alerts polling**: the sidenav badge polls `alertService.refreshUnreadCount()`
  every 15 seconds (immediate call + interval), exactly like the
  `interval(15000).pipe(startWith(0), switchMap(...))` in `layout.ts`.
- **Dashboard**: same KPI cards, same partial-failure handling (each
  data source falls back to `[]` independently instead of failing the whole
  dashboard).
- **Products / Suppliers**: same search + status filter logic, same
  activate/deactivate toggle, same snackbar messages on success/error.
- **Warehouses**: same stock table per warehouse (sorted by quantity desc),
  same Add/Deduct/Transfer dialogs, same "destination warehouse excludes
  itself" rule in the transfer dialog.
- **Purchase Orders**: same dynamic item rows (add/remove, can't go below 1
  row), same `currentUserId` resolution (from `localStorage.userId`, falling
  back to matching the logged-in email against the user list), same
  approve/receive/cancel visibility rules per status.
- **Movements**: same type/product/warehouse filters.
- **Alerts**: same unread count, mark-one/mark-all-read actions.
- **Reports**: same stock-value + low-stock breakdown tables and summary
  cards, same INR currency formatting.
- **Users**: same activate/deactivate action, same table columns.
- **Status chip colors** (`active`, `inactive`, `draft`, `approved`,
  `received`, `partially_received`, `cancelled`, `stock_in`, `stock_out`,
  `transfer`, `low_stock`, `po_overdue`, `manual`, `admin`, `manager`,
  `staff`) are byte-for-byte the same CSS as `styles.scss`.

## Necessary implementation-detail changes (not behavior changes)

These are unavoidable consequences of switching frameworks, not intentional
functional changes:

- RxJS `Observable`s → native `Promise`s (`async`/`await`), since React has no
  Angular-style DI/RxJS pipeline. Call sites read almost identically
  (`.subscribe({ next, error })` → `.then().catch()`).
- Angular `FormGroup`/`NgForm` validation → plain `useState` + manual
  required/min/pattern checks with the same touched/error semantics.
- Angular Material's `MatDialog.open(...).afterClosed()` → controlled MUI
  `<Dialog open={...}>` driven by component state.
- `[ngClass]`/`*ngIf`/`*ngFor` → JSX `className` template strings and
  `.map()`/conditional rendering — same resulting DOM/classes.
- Angular `currency`/`date` pipes → small formatter helpers in
  `shared/utils/format.ts` using `Intl.NumberFormat`/`Intl.DateTimeFormat`.

## Not ported: `*.spec.ts` files

Every Angular file had a matching `*.spec.ts`, but each one only contained the
CLI-generated boilerplate assertion (`expect(component).toBeTruthy()` /
`expect(executeGuard).toBeTruthy()`) with no real business-logic checks. Since
there was no behavior in them to preserve, and replicating them would require
pulling in React Testing Library solely to re-assert "it renders", they were
left out rather than mechanically translated. `npm test` (Vitest) is wired up
and ready if real tests are added later.

## Verified

- `npx tsc -b` — no type errors.
- `npm run build` — production build succeeds (Vite/Rollup).
- `npm run dev` — dev server boots on port 4200 and serves the app.
