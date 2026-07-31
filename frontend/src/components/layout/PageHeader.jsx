export function PageHeader({ surTitre, titre }) {
  return (
    <section className="border-b border-neutral-200 bg-white px-8 py-6">
      <div className="flex items-end justify-between gap-4">
        <div>
          <p className="mb-1 text-xxs font-semibold uppercase tracking-[0.2em] text-neutral-500">
            {surTitre}
          </p>
          <h1 className="text-2xl font-bold text-neutral-900">{titre}</h1>
        </div>
        <div
          className="hidden h-10 w-1 rounded-full sm:block"
          style={{ background: 'linear-gradient(180deg, #E30613, transparent)' }}
        />
      </div>
    </section>
  );
}
