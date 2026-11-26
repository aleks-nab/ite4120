import type { ReactElement } from 'react';
import { Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom';
import { Typography } from 'antd';
import { AppNavMenu } from '@helex/ui';
import { AnimalList } from './pages/AnimalList';
import { AnimalCreate } from './pages/AnimalCreate';
import { AnimalDetail } from './pages/AnimalDetail';
import { PlotCheck } from './pages/PlotCheck';
import { RuleList } from './pages/RuleList';
import { RuleDetail } from './pages/RuleDetail';
import { CoverageReport } from './pages/CoverageReport';

/**
 * Top-level navigation: land use first, the animals worked example kept last.
 * The active entry is the LONGEST matching path prefix, so the coverage report
 * (under /landuse/rules/) highlights itself, not "Restriction rules".
 */
const NAV_ITEMS = [
  { key: '/landuse/check', label: 'Plot check' },
  { key: '/landuse/rules', label: 'Restriction rules' },
  { key: '/landuse/rules/coverage-report', label: 'Coverage report' },
  { key: '/animals', label: 'Animals (example)' },
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
  const active = NAV_ITEMS.filter((i) => pathname.startsWith(i.key))
    .sort((a, b) => b.key.length - a.key.length)
    .slice(0, 1)
    .map((i) => i.key);

  return (
    <>
      <div style={{ padding: '12px 24px 0' }}>
        <Typography.Title level={4} style={{ margin: 0 }}>
          Land use restrictions · LANDUSE
        </Typography.Title>
        <Typography.Text type="secondary">
          Restrictions come from an imitated KPOIS register; rules are demo data, legal references to be verified.
        </Typography.Text>
      </div>
      <AppNavMenu
        mode="horizontal"
        selectedKeys={active}
        onClick={({ key }) => navigate(String(key))}
        items={NAV_ITEMS}
      />
      <Routes>
        <Route path="/" element={<Navigate to="/landuse/check" replace />} />
        <Route path="/animals" element={<AnimalList />} />
        <Route path="/animals/new" element={<AnimalCreate />} />
        <Route path="/animals/:id" element={<AnimalDetail />} />
        <Route path="/landuse/check" element={<PlotCheck />} />
        <Route path="/landuse/rules" element={<RuleList />} />
        {/* A static segment, so it outranks :id below; keeps "Restriction rules" highlighted. */}
        <Route path="/landuse/rules/coverage-report" element={<CoverageReport />} />
        {/* Also serves /landuse/rules/new — RuleDetail opens in create mode for id "new". */}
        <Route path="/landuse/rules/:id" element={<RuleDetail />} />
        {componentRoutes}
      </Routes>
    </>
  );
};
