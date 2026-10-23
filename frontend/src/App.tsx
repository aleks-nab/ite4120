import type { ReactElement } from 'react';
import { Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom';
import { AppNavMenu } from '@helex/ui';
import { AnimalList } from './pages/AnimalList';
import { AnimalCreate } from './pages/AnimalCreate';
import { AnimalDetail } from './pages/AnimalDetail';
import { PlotCheck } from './pages/PlotCheck';
import { RuleList } from './pages/RuleList';
import { RuleDetail } from './pages/RuleDetail';

/** Top-level navigation: one entry per page family; the active one is matched by path prefix. */
const NAV_ITEMS = [
  { key: '/animals', label: 'Animals' },
  { key: '/landuse/check', label: 'Plot check' },
  { key: '/landuse/rules', label: 'Restriction rules' },
];

/**
 * Components that may be ABSENT from this checkout register their routes by
 * exporting `routes: ReactElement[]` (a list of <Route>s) from
 * `pages/<component>/routes.tsx`. The glob is resolved at build time and is
 * simply empty when no such folder exists — the lecturer's private library
 * module lives there, gitignored. Students: add your <Route>s below, like animals.
 */
const componentRoutes = Object.values(
  import.meta.glob<{ routes: ReactElement[] }>('./pages/*/routes.tsx', { eager: true }),
).flatMap((m) => m.routes);

export const App = () => {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const active = NAV_ITEMS.filter((i) => pathname.startsWith(i.key)).map((i) => i.key);

  return (
    <>
      <AppNavMenu
        mode="horizontal"
        selectedKeys={active}
        onClick={({ key }) => navigate(String(key))}
        items={NAV_ITEMS}
      />
      <Routes>
        <Route path="/" element={<Navigate to="/animals" replace />} />
        <Route path="/animals" element={<AnimalList />} />
        <Route path="/animals/new" element={<AnimalCreate />} />
        <Route path="/animals/:id" element={<AnimalDetail />} />
        <Route path="/landuse/check" element={<PlotCheck />} />
        <Route path="/landuse/rules" element={<RuleList />} />
        {/* Also serves /landuse/rules/new — RuleDetail opens in create mode for id "new". */}
        <Route path="/landuse/rules/:id" element={<RuleDetail />} />
        {componentRoutes}
      </Routes>
    </>
  );
};
