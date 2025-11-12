(function() {
  'use strict';

  const FAQ_TEMPLATE = [
    '<div class="side-modal-content">',
    '  <div class="side-modal-header">',
    '    <h3><i class="bi bi-question-circle-fill"></i>Preguntas Frecuentes</h3>',
    '    <button class="close-modal" id="closeFaqModal" type="button" aria-label="Cerrar">&times;</button>',
    '  </div>',
    '  <div class="side-modal-body">',
    '    <p class="modal-intro">',
    '      <i class="bi bi-info-circle me-2" style="color: var(--turquesa-pastel);"></i>',
    '      Encuentra respuestas rápidas a las preguntas más comunes sobre nuestros servicios.',
    '    </p>',
    '    <div data-faq="true" data-accordion-id="faqAccordion" data-id-prefix="faq"></div>',
    '  </div>',
    '</div>'
  ].join('\n');

  function ensureFaqElements() {
    const body = document.body;
    if (!body) return {};

    let faqModal = document.getElementById('faqModal');
    if (!faqModal) {
      faqModal = document.createElement('div');
      faqModal.id = 'faqModal';
      faqModal.className = 'side-modal';
      body.appendChild(faqModal);
    } else if (!faqModal.classList.contains('side-modal')) {
      faqModal.classList.add('side-modal');
    }

    if (!faqModal.querySelector('[data-faq="true"]')) {
      faqModal.innerHTML = FAQ_TEMPLATE;
    }

    if (!faqModal.hasAttribute('role')) {
      faqModal.setAttribute('role', 'dialog');
    }
    faqModal.setAttribute('aria-modal', 'true');
    faqModal.setAttribute('aria-hidden', 'true');
    faqModal.tabIndex = -1;

    let floatingFaqBtn = document.getElementById('floatingFaqBtn');
    if (!floatingFaqBtn) {
      floatingFaqBtn = document.createElement('button');
      floatingFaqBtn.id = 'floatingFaqBtn';
      floatingFaqBtn.type = 'button';
      floatingFaqBtn.className = 'floating-faq-btn';
      floatingFaqBtn.title = 'Preguntas Frecuentes';
      floatingFaqBtn.setAttribute('aria-label', 'Abrir preguntas frecuentes');
      floatingFaqBtn.innerHTML = '<i class="bi bi-question-circle-fill"></i>';
      body.appendChild(floatingFaqBtn);
    }

    return { faqModal, floatingFaqBtn };
  }

  function initNavbarScroll() {
    window.addEventListener('scroll', function() {
      const navbar = document.querySelector('.navbar-overlay');
      if (!navbar) return;

      if (window.scrollY > 50) {
        navbar.classList.add('scrolled');
      } else if (!navbar.classList.contains('is-open')) {
        navbar.classList.remove('scrolled');
      }
    });
  }

  function initModals() {
    const elements = ensureFaqElements();
    const faqModal = elements.faqModal;
    const floatingFaqBtn = elements.floatingFaqBtn;
    const openFaqBtnMobile = document.getElementById('openFaqBtnMobile');

    if (!faqModal) return;

    const closeFaqModal = faqModal.querySelector('#closeFaqModal') || faqModal.querySelector('.close-modal');
    const faqContainer = faqModal.querySelector('[data-faq="true"]');
    let lastFocusedElement = null;

    const setAriaHidden = (isHidden) => {
      faqModal.setAttribute('aria-hidden', isHidden ? 'true' : 'false');
    };

    const ensureFaqContent = () => {
      if (!faqContainer || faqContainer.dataset.faqInitialized === 'true') return;
      if (typeof window.initAutoFAQ === 'function') {
        window.initAutoFAQ();
        faqContainer.dataset.faqInitialized = 'true';
      } else if (!faqContainer.innerHTML.trim()) {
        faqContainer.innerHTML = '<p class="text-muted">No se pudieron cargar las preguntas frecuentes.</p>';
        faqContainer.dataset.faqInitialized = 'true';
      }
    };

    const openFaqModal = (event) => {
      if (event) event.preventDefault();
      ensureFaqContent();
      faqModal.classList.add('active');
      document.body.style.overflow = 'hidden';
      setAriaHidden(false);
      lastFocusedElement = document.activeElement;
      const focusTarget = faqModal.querySelector('.close-modal');
      if (focusTarget) focusTarget.focus();
    };

    const closeFaqModalFunc = () => {
      faqModal.classList.remove('active');
      document.body.style.overflow = '';
      setAriaHidden(true);
      if (lastFocusedElement && typeof lastFocusedElement.focus === 'function') {
        lastFocusedElement.focus();
      } else if (floatingFaqBtn && typeof floatingFaqBtn.focus === 'function') {
        floatingFaqBtn.focus();
      }
    };

    if (floatingFaqBtn) {
      floatingFaqBtn.addEventListener('click', openFaqModal);
    }

    if (openFaqBtnMobile) {
      openFaqBtnMobile.addEventListener('click', openFaqModal);
    }

    if (closeFaqModal) {
      closeFaqModal.addEventListener('click', closeFaqModalFunc);
    }

    faqModal.addEventListener('click', function(e) {
      if (e.target === faqModal) {
        closeFaqModalFunc();
      }
    });

    window.addEventListener('keydown', function(e) {
      if (e.key === 'Escape' && faqModal.classList.contains('active')) {
        closeFaqModalFunc();
      }
    });

    window.closeFaqModalFunc = closeFaqModalFunc;
  }

  function init() {
    initNavbarScroll();
    initModals();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
